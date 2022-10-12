class Trie:

    def __init__(self):
        self.root = {}
        self.word_end = -1
        self.label = 'label'

    def insert(self, word, label):
        """
        Inserts a word into the trie.
        :type word: str
        :rtype: void
        """
        curNode = self.root
        for c in word:
            if not c in curNode:
                curNode[c] = {}
            curNode = curNode[c]

        curNode[self.word_end] = True
        if self.label not in curNode:
            curNode[self.label] = set()
        curNode[self.label].add(label)

    def search(self, word):
        """
        Returns if the word is in the trie.
        :type word: str
        :rtype: bool
        """
        curNode = self.root
        for c in word:
            if not c in curNode:
                return (False, None)
            curNode = curNode[c]
        if self.word_end not in curNode:
            return (False, None)

        return (True, curNode[self.label])

    def startsWith(self, prefix):
        """
        Returns if there is any word in the trie that starts with the given prefix.
        :type prefix: str
        :rtype: bool
        """
        curNode = self.root
        for c in prefix:
            if not c in curNode:
                return False
            curNode = curNode[c]

        return True
