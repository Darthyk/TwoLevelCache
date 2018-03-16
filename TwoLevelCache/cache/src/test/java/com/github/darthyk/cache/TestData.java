package com.github.darthyk.cache;

public class TestData {
    public static final int DEFAULT_CAPACITY = 3;

    public enum IntegerData {
        FIRST(1, 3),
        SECOND(2, 6),
        THIRD(3, 647),
        FOURTH(4, 3564),
        FIFTH(5, 23478);

        private Integer key;
        private Integer value;

        private IntegerData(Integer key, Integer value) {
            this.key = key;
            this.value = value;
        }

        public Integer getKey(){
            return this.key;
        }

        public Integer getValue() {
            return this.value;
        }
    }

    public enum StringData {
        FIRST(1, "value1"),
        SECOND(2, "value2"),
        THIRD(3, "value3"),
        FOURTH(4, "value4"),
        FIFTH(5, "value5");

        private Integer key;
        private String value;

        private StringData(Integer key, String value) {
            this.key = key;
            this.value = value;
        }

        public Integer getKey(){
            return this.key;
        }

        public String getValue() {
            return this.value;
        }
    }
}
