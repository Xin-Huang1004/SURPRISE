/*
 * Copyright 2017 CMPUT301W17T07
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmput301w17t07.moody;

import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by mike on 2017-02-23.
 */

public class ProfileController {


    //private static JestDroidClient client;

    public class AddProfile extends AsyncTask<User, Void, Void> {
        @Override
        protected Void doInBackground(User... params) {
                return null;
            }
    }

    public class GetProfile extends AsyncTask<String, Void, ArrayList<User>> {
        @Override
        protected ArrayList<User> doInBackground(String... params) {
            verifySettings();
            return null;
        }
    }

    public void verifySettings() {

    }

}
