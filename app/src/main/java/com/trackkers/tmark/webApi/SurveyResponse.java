package com.trackkers.tmark.webApi;

import java.util.List;

public class SurveyResponse {

    private String status;
    private String msg;
    private List<DataBean> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {

        private String surveyname;
        private int survuid;
        private List<AllQuestionsBean> allQuestions;

        public String getSurveyname() {
            return surveyname;
        }

        public void setSurveyname(String surveyname) {
            this.surveyname = surveyname;
        }

        public int getSurvuid() {
            return survuid;
        }

        public void setSurvuid(int survuid) {
            this.survuid = survuid;
        }

        public List<AllQuestionsBean> getAllQuestions() {
            return allQuestions;
        }

        public void setAllQuestions(List<AllQuestionsBean> allQuestions) {
            this.allQuestions = allQuestions;
        }

        public static class AllQuestionsBean {

            private int quid;
            private String question;
            private List<OptionsBean> options;

            public int getQuid() {
                return quid;
            }

            public void setQuid(int quid) {
                this.quid = quid;
            }

            public String getQuestion() {
                return question;
            }

            public void setQuestion(String question) {
                this.question = question;
            }

            public List<OptionsBean> getOptions() {
                return options;
            }

            public void setOptions(List<OptionsBean> options) {
                this.options = options;
            }

            public static class OptionsBean {
                /**
                 * ouid : 42
                 * quid : 17
                 * option : sv1opt1
                 */

                private int ouid;
                private int quid;
                private String option;

                public int getOuid() {
                    return ouid;
                }

                public void setOuid(int ouid) {
                    this.ouid = ouid;
                }

                public int getQuid() {
                    return quid;
                }

                public void setQuid(int quid) {
                    this.quid = quid;
                }

                public String getOption() {
                    return option;
                }

                public void setOption(String option) {
                    this.option = option;
                }
            }
        }
    }
}
