package xyz.scootaloo.console.app.config;

import lombok.Getter;
import xyz.scootaloo.console.app.config.ConsoleConfigProvider.DefaultValueConfigBuilder;

/**
 * 作者信息
 * @author flutterdash@qq.com
 * @since 2021/1/1 12:30
 */
@Getter
public class Author {

    private final DefaultValueConfigBuilder dvBuilder;
    protected String name       = "";
    protected String email      = "";
    protected String createDate = "";
    protected String updateDate = "";
    protected String comment    = "";

    public Author(DefaultValueConfigBuilder dvBuilder) {
        this.dvBuilder = dvBuilder;
    }

    public Author authorName(String name) {
        if (name != null) {
            this.name = name;
        }
        return this;
    }

    public Author email(String email) {
        if (email != null) {
            this.email = email;
        }
        return this;
    }

    public Author createDate(String date) {
        if (date != null) {
            this.createDate = date;
        }
        return this;
    }

    public Author updateDate(String date) {
        if (date != null) {
            this.updateDate = date;
        }
        return this;
    }

    public Author comment(String comment) {
        if (comment != null) {
            this.comment = comment;
        }
        return this;
    }

    public DefaultValueConfigBuilder ok() {
        return this.dvBuilder;
    }

}
