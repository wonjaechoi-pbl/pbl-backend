package com.gogofnd.kb.global.config.firebase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FirebaseRequest {
    private String to;
    private Notification notification;
}

@AllArgsConstructor
@NoArgsConstructor
@Getter
class Notification {
    private String body;
    private String title;

    @Override
    public String toString() {
        return "{" +
                "body='" + body + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
