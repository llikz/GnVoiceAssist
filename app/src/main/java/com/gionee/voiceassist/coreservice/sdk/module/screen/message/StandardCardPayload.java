package com.gionee.voiceassist.coreservice.sdk.module.screen.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StandardCardPayload extends RenderCardPayload {
    private String token;
    private String type;
    private Image image;
    private String title;
    private String content;
    private Link link;

    public StandardCardPayload(@JsonProperty("token") String token, @JsonProperty("type") String type, @JsonProperty("image") Image image, @JsonProperty("title") String title, @JsonProperty("content") String content, @JsonProperty("link") Link link)
    {
        this.token = token;
        this.type = type;
        this.image = image;
        this.title = title;
        this.content = content;
        this.link = link;
    }

    public String getToken()
    {
        return this.token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getType()
    {
        return this.type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Image getImage()
    {
        return this.image;
    }

    public void setImage(Image image)
    {
        this.image = image;
    }

    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return this.content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

}
