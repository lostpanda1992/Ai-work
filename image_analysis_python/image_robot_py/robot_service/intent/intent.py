import re
from functools import reduce
from robot_service.intent.Trie import Trie


class intent(object):
    def __init__(self, dic_path, pat_path, stdq_path):
        self.dic_path = dic_path
        self.pat_path = pat_path
        self.stdq_path = stdq_path
        self.dic = {}
        self.stdq = {}
        self.fstPacIndex = {}
        self.trie = Trie()
        self.negWords = ['不要', '不是', '非']

        self.loadDic()

    #  加载业务Q和名字
    def loadStdq(self):
        with open(self.stdq_path, encoding='utf8') as f:
            for line in f:
                l = line.strip().split('\t')
                if len(l) < 2:
                    continue
                qid, qname = l[0], l[1]
                self.stdq[qid] = qname

    # 词典构建trie
    def buildTrie(self):
        for label in self.dic:
            for term in self.dic[label]:
                self.trie.insert(term, label)

    # 读自定义词典
    def readDic(self):
        with open(self.dic_path, encoding='utf8') as f:
            label = ''
            terms = set()
            for line in f:
                l = line.strip()
                if l.startswith('#'):
                    if label != '' and len(terms) != 0:
                        self.dic[label] = terms
                    label = l.split('#')[1]
                    terms = set()
                else:
                    terms.add(l)
            if label != '' and len(terms) != 0:
                self.dic[label] = terms
        self.buildTrie()

    # 排列展示pat所有组合
    def getAllComb(self, patList, code=''):
        fn = lambda xx, code: reduce(lambda x, y: [str(i) + code + str(j) for i in x for j in y], xx)
        return fn(patList, code)

    # 构架pat首元素索引
    def buildFstPacIndex(self, patList, bizId, score):
        for pat in patList:
            labelList = [x for x in pat.split('#') if x != '']
            if len(labelList) != 0:
                fstPat = labelList[0]
                if fstPat not in self.fstPacIndex:
                    self.fstPacIndex[fstPat] = []
                self.fstPacIndex[fstPat].append((labelList[0:], bizId, score))

    # 解析pat
    def readPat(self):
        with open(self.pat_path, encoding='utf8') as f:
            for line in f:
                # 过滤掉#开头的注释
                if line.startswith('#'):
                    continue
                l = line.strip().split(',')
                if len(l) < 5:
                    continue
                patId, pat, bizId, score, info = l[0], l[1], l[2], l[3], l[4]
                subPats = re.split('\(|\)', pat)
                tmpList = []
                for p in subPats:
                    if p != '':
                        tmpList.append(re.split('\|', p))
                self.buildFstPacIndex(self.getAllComb(tmpList), bizId, score)

    # 包含否定词
    def containNeg(self, sentence):
        indexList = []
        for neg in self.negWords:
            if neg in sentence:
                indexList.append(sentence.index(neg))
        if len(indexList) == 0:
            return None
        else:
            return min(indexList)

    # 处理否定词
    # 如果有多个子句，删除出现否定词的字句；
    # 如果只有一个子句，删除否定词后边子串
    def delNegWords(self, sentence):
        subSentence = re.split('[\s,\?，！。？]', sentence)
        subSentence = [s for s in subSentence if s != '']
        if len(subSentence) > 1:
            subSentence = [s for s in subSentence if self.containNeg(s) == None]
        elif len(subSentence) == 1:
            tmpS = subSentence[0]
            if self.containNeg(tmpS) != None:
                subSentence = [tmpS[:self.containNeg(tmpS)]]
        return ' '.join(subSentence)

    # 分词、打标
    # 输出所有标签的集合以及标签在句子中出现过的所有位置
    def cut(self, sentence):
        # 存储打出来的标签及在句子出现的所有位置
        result = {}
        # 删除否定词
        sentence = self.delNegWords(sentence)
        senLen = len(sentence)
        # 从左到右，每个字符，都要向后查找trie是否可以继续往下查找。
        # 如果trie树是个前缀，继续向下查找，直到不是前缀。
        # 注意：此时，可能是个短的词被包含了，比如：错、错误，查到第一个后需要继续往后查找。
        for i in range(senLen):
            for j in range(i + 1, senLen + 1):
                term = sentence[i:j]
                if self.trie.startsWith(term):
                    isTerm, labels = self.trie.search(term)
                    if isTerm:
                        # print(term, i, ','.join(labels))
                        for lb in labels:
                            if lb not in result:
                                result[lb] = set()
                            result[lb].add(str(i))
                else:
                    break
        return result

    # 数组是否是从小到大有序
    def isSorted(self, iterable, key=lambda x, y: x <= y):
        cmpFunc = lambda x, y: y if key(x, y) else float('inf')
        return reduce(cmpFunc, iterable, .0) < float('inf')

    # 匹配模板
    # 返回业务问题id；如果没匹配上，返回原因
    def matchPat(self, labelIndex):
        # 1.取出所有句子中标签开头模板，过滤出一部分候选集
        fstPatInfo = []
        patsList = []
        for label in labelIndex:
            if label in self.fstPacIndex:
                fstPatInfo.append(self.fstPacIndex[label])
        if len(fstPatInfo) == 0:
            return None, 'No first label pat'
        # 2.看看模板中每个元素是否都在句子出现，扔掉一部分候选集
        for data in fstPatInfo:
            for pats, bizId, score in data:
                inFlag = True
                for p in pats:
                    if p not in labelIndex:
                        inFlag = False
                        break
                if inFlag:
                    patsList.append((pats, bizId, score))
        if len(patsList) == 0:
            return None, 'Sentence label not all match pat'
        # 3.最后检查剩下的候选集，存不存在模板和句子中标签顺序一致
        # 假设模板里的元素必须是严格有序的
        tmpResult = []
        for pat, bizId, score in patsList:
            indexList = []
            for p in pat:
                indexList.append(list(labelIndex[p]))
            indexComb = self.getAllComb(indexList, ',')
            for indexPath in indexComb:
                idx = [int(i) for i in indexPath.split(',')]
                if self.isSorted(idx):
                    tmpResult.append((bizId, score))
                    break
        # 得到最终结果，返回匹配到的业务id
        if len(tmpResult) > 0:
            sd = sorted(tmpResult, key=lambda x: x[1], reverse=True)
            sd = sd[0]
            bisId = sd[0]
            bizInfo = bisId
            if bizInfo in self.stdq:
                bizInfo = self.stdq[bizInfo]
            return bizInfo, str(sd[1])
        else:
            return None, 'Pat label order not match'

    # 加载自定义标签词典及模板文件
    def loadDic(self):
        self.readDic()
        self.readPat()
        self.loadStdq()
