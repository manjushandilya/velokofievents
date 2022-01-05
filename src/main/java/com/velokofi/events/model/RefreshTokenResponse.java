package com.velokofi.events.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RefreshTokenResponse {

    private String token_type;

    private String access_token;

    private long expires_at;

    private long expires_in;

    private String refresh_token;
}
