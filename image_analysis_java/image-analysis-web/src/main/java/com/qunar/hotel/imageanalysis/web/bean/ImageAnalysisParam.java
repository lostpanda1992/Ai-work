package com.xxxxxx.hotel.imageanalysis.web.bean;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;


@Data
public class ImageAnalysisParam implements Cloneable {


    private String appCode;

    private String traceId;



    private String serviceName;
    private List<ImageDetectResult> data = Lists.newArrayList();

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<ImageDetectResult> getData() {
        return data;
    }

    public void setData(List<ImageDetectResult> data) {
        this.data = data;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"serviceName\":\"")
                .append(serviceName).append('\"');
        sb.append(",\"data\":")
                .append(data);
        sb.append('}');
        return sb.toString();
    }

    class ImageDetectResult {

        private String originalId;
        private String imageId;
        private String source;
        private Integer detectStatus;
        private String jsonReuslt;

        public String getOriginalId() {
            return originalId;
        }

        public void setOriginalId(String originalId) {
            this.originalId = originalId;
        }

        public String getImageId() {
            return imageId;
        }

        public void setImageId(String imageId) {
            this.imageId = imageId;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public Integer getDetectStatus() {
            return detectStatus;
        }

        public void setDetectStatus(Integer detectStatus) {
            this.detectStatus = detectStatus;
        }

        public String getJsonReuslt() {
            return jsonReuslt;
        }

        public void setJsonReuslt(String jsonReuslt) {
            this.jsonReuslt = jsonReuslt;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("\"originalId\":\"")
                    .append(originalId).append('\"');
            sb.append(",\"imageId\":\"")
                    .append(imageId).append('\"');
            sb.append(",\"source\":\"")
                    .append(source).append('\"');
            sb.append(",\"detectStatus\":")
                    .append(detectStatus);
            sb.append(",\"jsonReuslt\":\"")
                    .append(jsonReuslt).append('\"');
            sb.append('}');
            return sb.toString();
        }
    }


}
