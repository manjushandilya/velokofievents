package com.velokofi.events.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AthleteProfile {

    /*
    {
      "id": 1234567890987654400,
      "username": "marianne_t",
      "resource_state": 3,
      "firstname": "Marianne",
      "lastname": "Teutenberg",
      "city": "San Francisco",
      "state": "CA",
      "country": "US",
      "sex": "F",
      "premium": true,
      "created_at": "2017-11-14T02:30:05Z",
      "updated_at": "2018-02-06T19:32:20Z",
      "badge_type_id": 4,
      "profile_medium": "https://xxxxxx.cloudfront.net/pictures/athletes/123456789/123456789/2/medium.jpg",
      "profile": "https://xxxxx.cloudfront.net/pictures/athletes/123456789/123456789/2/large.jpg",
      "friend": null,
      "follower": null,
      "follower_count": 5,
      "friend_count": 5,
      "mutual_friend_count": 0,
      "athlete_type": 1,
      "date_preference": "%m/%d/%Y",
      "measurement_preference": "feet",
      "clubs": [],
      "ftp": null,
      "weight": 0,
      "bikes": [
        {
          "id": "b12345678987655",
          "primary": true,
          "name": "EMC",
          "resource_state": 2,
          "distance": 0
        }
      ],
      "shoes": [
        {
          "id": "g12345678987655",
          "primary": true,
          "name": "adidas",
          "resource_state": 2,
          "distance": 4904
        }
      ]
    }
     */

    private long id;

    private String username;
    private String firstname;
    private String lastname;
    private String sex;
    private String profile_medium;
    private String profile;

}
