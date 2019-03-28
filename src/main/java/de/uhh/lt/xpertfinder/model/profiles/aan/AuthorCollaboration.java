package de.uhh.lt.xpertfinder.model.profiles.aan;

import java.math.BigInteger;

public class AuthorCollaboration {

    private String name;
    private int count;
    private long id;

    public AuthorCollaboration(Object[] collaboration) {
        this.name = (String) collaboration[0];
        this.count = (int) collaboration[1];
        this.id = ((BigInteger)collaboration[2]).longValue();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
