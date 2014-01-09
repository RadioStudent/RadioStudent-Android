package com.radiostudent.radiostudentstream;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressLint({"NewApi"})
public class ParserM3UToURL
{
public static String parse(String paramString, int sdkVersion, Context c)
{
	try {
	    StrictModeWrapper.init(c);
	}
	catch(Throwable throwable) {
	    //Toast.makeText(c, "is not available. Punting...", Toast.LENGTH_LONG).show();
	}
	/*if(sdkVersion < android.os.Build.VERSION_CODES.JELLY_BEAN) {
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .penaltyLog()
        .penaltyDeath()
        .build());
	}
	else {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
	}*/
    try
    {
      HttpURLConnection localHttpURLConnection = (HttpURLConnection)new URL(paramString).openConnection();
      InputStream localInputStream = localHttpURLConnection.getInputStream();
      BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream));
      StringBuffer localStringBuffer = new StringBuffer();
      while (true)
      {
        String str = localBufferedReader.readLine();
        if (str == null)
        {
          localHttpURLConnection.disconnect();
          localBufferedReader.close();
          localInputStream.close();
          break;
        }
        if (str.contains("http"))
        {
          localHttpURLConnection.disconnect();
          localBufferedReader.close();
          localInputStream.close();
          return str;
        }
        localStringBuffer.append(str);
      }
    }
    catch (MalformedURLException localMalformedURLException)
    {
      localMalformedURLException.printStackTrace();
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return null;
  }
}