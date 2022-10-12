//package com.xxxxxx.hotel.feedsearch.web.es;
//
//import com.xxxxxx.hotel.feedsearch.web.AppTest;
//import io.searchbox.client.http.JestHttpClient;
//import io.searchbox.core.Search;
//import io.searchbox.core.SearchResult;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.common.unit.TimeValue;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.junit.Test;
//
//import javax.annotation.Resource;
//
//@Slf4j
//public class TestEs  extends AppTest {
//
//    @Resource(name = "contentSearchJestHttpClient")
//    private JestHttpClient contentSearchJestHttpClient;
//
//    private static final String[] INCLUDE_FEILDS = {"globalKey", "totalWeighting", "likeWeighting","clickWeighting","positionWeighting", "originCollectLikeWeight", "headDiagramWordPress","createTime", "likeNum", "mediaType", "cityName", "poiType", "firstLevelTagNames", "travelWeight"};
//
//    @Test
//    public void test_cityName()  {
//        String cityName = "北京";
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        searchSourceBuilder.size(100);
//        searchSourceBuilder.timeout(new TimeValue(1000));
//
//        searchSourceBuilder.fetchSource(INCLUDE_FEILDS, new String[]{});
//
//        BoolQueryBuilder queryBulder = QueryBuilders.boolQuery();
//
//        MatchPhraseQueryBuilder titleQueryUn = QueryBuilders.matchPhraseQuery("title", cityName).slop(50).boost(1);
//        queryBulder.should(titleQueryUn);
//
//        MatchPhraseQueryBuilder poiNmaeQueryUn = QueryBuilders.matchPhraseQuery("poiName", cityName).slop(50).boost(1);
//        queryBulder.should(poiNmaeQueryUn);
//
//        MatchPhraseQueryBuilder contentQueryUn = QueryBuilders.matchPhraseQuery("content", cityName).slop(50).boost(1);
//        queryBulder.should(contentQueryUn);
//
//        searchSourceBuilder.query(queryBulder);
//
//        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("content_search").addType("doc").build();
//        SearchResult searchResult = null;
//        try {
//            searchResult = contentSearchJestHttpClient.execute(search);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(searchResult);
//
//    }
//
//
//}
