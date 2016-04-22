/**
 *  Radio for android, internet radio.
 *
 * Copyright (C) 2016 Old Geek
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.oucho.radio2.net;

import org.oucho.radio2.utils.Playlist;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.List;
import java.util.ArrayList;

public class HttpGetter {

   public static List<String> httpGet(String str) {

      HttpURLConnection connection = null;
      List<String> lines = new ArrayList<>();

      try {

         URL url = new URL(str);
         connection = (HttpURLConnection) url.openConnection();

         if ( Playlist.isPlaylistMimeType(connection.getContentType()) ) {
            InputStream stream = new BufferedInputStream(connection.getInputStream());
            readStream(stream, lines);
         }
         connection.disconnect();
      } catch ( Exception e ) {
         if ( connection != null )
            connection.disconnect();
      }

      return lines;
   }

   private static void readStream(InputStream stream, List<String> lines) throws Exception {

      String line;
      BufferedReader buff = new BufferedReader(new InputStreamReader(stream));

      while ((line = buff.readLine()) != null)
         lines.add(line);

      stream.close();
   }
}
