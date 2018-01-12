package com.example.automaticvideodirector.ui;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/*
 * This is a custom cookie storage for the application. This
 * will store all the cookies to the shared preferences so that it persists
 * across application restarts.
 * http://stackoverflow.com/questions/12349266/how-do-i-persist-cookies-when-using-httpurlconnection
 */
class MyCookieStore implements CookieStore {

	/*
	 * The memory storage of the cookies
	 */
	private Map<String, List<HttpCookie>> mapCookies = new HashMap<String, List<HttpCookie>>();
	/*
	 * The instance of the shared preferences
	 */
	private final SharedPreferences spePreferences;

	/*
	 * @see java.net.CookieStore#add(java.net.URI, java.net.HttpCookie)
	 */
	public void add(URI uri, HttpCookie cookie) {

		System.out.println("add");
		System.out.println(cookie.toString());

		List<HttpCookie> cookies = mapCookies.get(uri.getHost());
		System.out.println(uri.getHost());
		if (cookies == null) {
			cookies = new ArrayList<HttpCookie>();
			mapCookies.put(uri.getHost(), cookies);
		}
		cookies.add(cookie);

		Editor ediWriter = spePreferences.edit();
		HashSet<String> setCookies = new HashSet<String>();
		setCookies.add(cookie.toString());
//		ediWriter.putStringSet(uri.getHost(), spePreferences.getStringSet(uri.getHost(), setCookies));
		ediWriter.putStringSet(uri.getHost(), setCookies);
		ediWriter.commit();

	}

	/*
	 * Constructor
	 * 
	 * @param  ctxContext the context of the Activity
	 */
	@SuppressWarnings("unchecked")
	public MyCookieStore(Context ctxContext) {
		
		spePreferences = ctxContext.getSharedPreferences("CookiePrefsFile", Context.MODE_PRIVATE);
//		spePreferences = PreferenceManager.getDefaultSharedPreferences(ctxContext);
		Map<String, ?> prefsMap = spePreferences.getAll();

		for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {

			for (String strCookie : (HashSet<String>) entry.getValue()) {

				if (!mapCookies.containsKey(entry.getKey())) {

					List<HttpCookie> lstCookies = new ArrayList<HttpCookie>();
					lstCookies.addAll(HttpCookie.parse(strCookie));

					mapCookies.put(entry.getKey(), lstCookies);

				} else {

					List<HttpCookie> lstCookies = mapCookies.get(entry.getKey());
					lstCookies.addAll(HttpCookie.parse(strCookie));

					mapCookies.put(entry.getKey(), lstCookies);

				}

				System.out.println(entry.getKey() + ": " + strCookie);

			}

		}

	}

	/*
	 * @see java.net.CookieStore#get(java.net.URI)
	 */
	public List<HttpCookie> get(URI uri) {

		List<HttpCookie> lstCookies = mapCookies.get(uri.getHost());
		System.out.println(uri.getHost());
		
		if (lstCookies == null) {
			mapCookies.put(uri.getHost(), new ArrayList<HttpCookie>());
			
		}
		System.out.println(mapCookies.get(uri.getHost()));
		return mapCookies.get(uri.getHost());

	}

	/*
	 * @see java.net.CookieStore#removeAll()
	 */
	public boolean removeAll() {

		mapCookies.clear();
		Editor ediWriter = spePreferences.edit();
		ediWriter.clear();
		ediWriter.commit();
		return true;

	}

	/*
	 * @see java.net.CookieStore#getCookies()
	 */
	public List<HttpCookie> getCookies() {

		Collection<List<HttpCookie>> values = mapCookies.values();

		List<HttpCookie> result = new ArrayList<HttpCookie>();
		for (List<HttpCookie> value : values) {
			result.addAll(value);
		}

		return result;

	}

	/*
	 * @see java.net.CookieStore#getURIs()
	 */
	public List<URI> getURIs() {

		Set<URI> keys = new HashSet<URI>();
		for(String key : mapCookies.keySet()) {
			try {
				keys.add(new URI(key));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return new ArrayList<URI>(keys);

	}

	/*
	 * @see java.net.CookieStore#remove(java.net.URI, java.net.HttpCookie)
	 */
	public boolean remove(URI uri, HttpCookie cookie) {

		List<HttpCookie> lstCookies = mapCookies.get(uri.getHost());

		if (lstCookies == null)
			return false;

		return lstCookies.remove(cookie);

	}

}