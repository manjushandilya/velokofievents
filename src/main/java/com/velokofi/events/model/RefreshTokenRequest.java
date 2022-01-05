package com.velokofi.events.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RefreshTokenRequest {

    private String client_id;

    private String client_secret;

    private String grant_type;

    private String refresh_token;

}
