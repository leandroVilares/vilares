package com.ml.logistica.romaneio.entity;

public class PackingListType {

    private Long id;
    private String name;

    private PackingListType(Builder builder) {
        setId(builder.id);
        setName(builder.name);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(PackingListType copy) {
        Builder builder = new Builder();
        builder.id = copy.id;
        builder.name = copy.name;
        return builder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static final class Builder {
        private Long id;
        private String name;

        private Builder() {
        }

        public Builder id(Long val) {
            id = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public PackingListType build() {
            return new PackingListType(this);
        }
    }
}
