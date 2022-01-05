package com.velokofi.events.model.hungryvelos;

public interface TeamConstants {

    String TEAMS_CSV = """
            team_id, name, captain_strava_id
            1, Bangadi Warriors, 43378948
            2, Chakra Chalukyas, 6848467
            3, Hoysalas, 75500724
            4, Vijayanagara Velos, 19218746
            """;

    String TEAM_MEMBERS_CSV = """
            strava_id, name, alias, gender, team_id, captain
            43378948, Shruthi Subbanna, Shruthi, F, 1, true
            35940578, Sai Srinivas, Sai, M, 1, false
            37177283, Manjunath Sathyanarayana, Manjax, M, 1, false
            28234693, Goutham YV, Goutham, M, 1, false
            74963850, Nandini Kishor, Nandu, F, 1, false
            69245380, Siddharth Horee, Sid, M, 1, false
            14463022, Sagar MC, Sagar, M, 1, false
            3225831, Prashanth S, Prashanth, M, 1, false
            73010512, Chandrashekar K, CK, M, 1, false
            15589851, Sun'J Sharma, Sun'J, M, 1, false
            3377091, Sunil Gopala Chari, Sunil G, M, 1, false
            9125629, Deepu Scorpio ( B+Ve ), Deepu, M, 1, false
            37178147, Sandeep B N, Sandeep, M, 1, false
            58389372, Rupesh 🏃🚴🚵🏊, Rupesh, M, 1, false
            6848467, Shreenivasa K s, Shrini, M, 2, true
            62516649, Anil Kumar, Anil, M, 2, false
            20378999, Rav Indra, Rav Indra, M, 2, false
            62411206, Arun Bhaskar, Arun Bhaskar, M, 2, false
            75083256, Manu Gopinath, Manu, M, 2, false
            38684955, Premchand Ryali, Prem, M, 2, false
            83728576, Shyam Sundar K, Shyam, M, 2, false
            12244586, Kirti Chalam, Kirti, F, 2, false
            64757576, Bharadwaja S R, Pradeepa, M, 2, false
            78896766, Rahul Padmanabha, Rahul, M, 2, false
            69757350, Ravi Sunderrajan, Ravi, M, 2, false
            51400681, Roopa Rupesh, Roopa, F, 2, false
            17249418, Chandan Gaddehosur, Chandan, M, 2, false
            75500724, Revathi M P, Revs, F, 3, true
            65820756, Nanda Kishor G D, Kishor, M, 3, false
            65392239, Vipin Devis, Vipin, M, 3, false
            64907699, Amarnath Vali, Amari, M, 3, false
            63014939, Srinidhi Bharadwaj, Nidhi, M, 3, false
            61999014, Vidyaprasanna Kambalur, VP, M, 3, false
            61335755, Sampige Santhosh, Santhosh, M, 3, false
            68355712, Sandeep Vishwanath, Sandeep, M, 3, false
            33590187, Faraz Umar, Faraz, M, 3, false
            25409226, Lakshmi Narasu, Laks, F, 3, false
            16222927, Mythri Sunil, Mythri, F, 3, false
            19218746, Raghu B V, Raghu, M, 4, true
            85387376, Nischal Kumar, Nischal, M, 4, false
            74710582, Zainab Shoaib, Zainab, F, 4, false
            74590313, Venkat, Dr Venkat, M, 4, false
            73629852, Bharathi Malavalli, Bharu, F, 4, false
            73492491, Savitha Naik, Savitha, F, 4, false
            71154685, Ashwini Nischal (Ashu), Ashwini, F, 4, false
            70101578, Chidanand Kawri Lokesh, Chidu, M, 4, false
            67288162, Harsha Gandhi, Harsha, F, 4, false
            62471947, Girish Jain Jain, Girish, M, 4, false
            38148750, Chandrakanth, Chandu, M, 4, false
            33762572, Arun Bastin, Bastin, M, 4, false
            28550167, Sudarshan Gopinath, Sudu, M, 4, false
            """;
}
