import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpTest {

	public static void main(String[] args) throws ClientProtocolException, IOException {
		@SuppressWarnings("deprecation")
		DefaultHttpClient httpClient = new DefaultHttpClient();
        String urlOverHttps
          = "https://uat.citivelocity.com/hubsearch2/json?q=YWFwbA&qencode_schema=base64&start=1&end=10&pubType=research,commentary&platformID=1&model=nonbetamodel-04282017&enrich-facet=Company,Author,Region,EMRegions";
//        CookieStore cookieStore = new BasicCookieStore(); 
//        BasicClientCookie cookie = new BasicClientCookie("SMSESSION", "aHTj666eWG/eYc2WMWkDVIZLua4kJ7HCLF0JUJFD7eE1OhgXV4sP6Cxj8yQeEjKqZcGfAdKnjjUUEFgmqnBXs6uQKfXSlHMAsGCCexcpUt307pTrA5XW/OFrm6HbGSFL+scSmsy+hBjwdY28BnIu1LlTB9+Fd4fs1OW2RZ1qqTifwQN7RlDt+mStNeMv9rjpo+RrpHdbyfcyHlkRnTogp+gK+TWbKdm2PJAd1tYRXu/p05CXZWATFQ15ifOJI/cIJqS1fsjqKZBNxwJWBbTkG++UwM9l3ecIp02DrnUPTkPUDZTyw46xrQXi3KdfV7KpWLYTLsT1Zlfy4cTuYDrwYVWfU6m6yB7smpej4Ao2PPGgRNFBADEmBtEYWeLyZo2QBRK3hko8QCZqzYyUUfvRheC4SeeuCAU4VRLVPZcgB8+CQpjrj59/xP9Fqv/Tyi1dYErtfkVAu3LlUQ60V/VloFvXgXIbt5nqGoTJKUkRDYliOgfyymvsJU9xEaOe6CLjcEmaiACavBhxLGfa3MTDyPYQ4RzrO2svLFS+Ty8/0df36F3VwLof/IopY+GGBEt13L+YMJufuCeA81bjflDvNfbicOeUoU6wjAobPrvzgL2I9un7BWGwUKI53Ip/qMEveHU8fgN55BA5vmYotX4rTWjLNlDEYzc3TGAwUyg5Cabvxhd50iZ0W0zwACvnmFNyNbqklWH4/+iJvo21GmWNwbyR8tmfSGFboZ1IMOX3RdSawGjiyZKMXcdQ9wR0FDfPXAst1jWiG17XsyWENS80UQ8xcXCuEL3yUwCWlHRSlVGKR2iHbnohY7w1L8jFiMwcVeEsMPxdgR8NPZLzKRZUKTChE0IWsl9d1FtE2hyXaOG4jzwi2YMBXpnbLNIDNchqQkKXFlkK0tdZHY6XpZFgT0Nw7BgKG6oSOTHoiyJMKtT6c/g0mYphx+823A0g9KWzK8pEEZEg4ertdWTFMZPTIKT4uCGty3yjAEKEb9taXaXr0RtO6veQh1krRpljN8jqkcKS7qpnFAxgF0fdHSwS16fm9MXOc860TrumU6pv/QjfO4Cm6DsoDCJYgXg6o1l/");
        
        HttpGet getMethod = new HttpGet(urlOverHttps);
         getMethod.setHeader("Cookie", "SMSESSION=QctpMvbebjWk0ZlpqjbToZt1/j7BC/AOdT88NxndCZpGr6BqwiaL8QJgkdrFyksDhVUNyZ/CaJg72lRTux+mVxuN4FoG2xKlQy5a8sC+FwwGX5+RCCbN9ZnNs32Z7YshOaWi3d2wmV14DW6oR6Y59it0cRNY0Or1HQTIIBynEFhu7z5MXKkyOlhZPoC3em8aRrCKVWPVG6VLrWzFUnsA1j1XQoKlIMwdM2yIZz4jpuG+uxJpKhh1K1Nja6+JAvG3Rnq7di6pbBxByLw9aVI6kGEVIIa0b6s8zGWMLsH7jSP5n7lN/Jv4KOMEZAnksdYRGEaBVuxaesZuIfmPxuXQcli8PyFIUIHXaaMgf5zjYDdanF/VTBbEDfPMKPWeMbigBkL3/fvU7+ITwbKzIeaHspAdAMew7bfx1hHLHDr+u3knoR93v2kX/qyzPPs8GdKdxm3UWO/1vINKH801C5IMxXxGVz+4hJjMBzSODk5OeUkDpGo9sDw0yr/RDodJUWW75p9tvCaZCyUWliFsJiB9ytwyTvE9tJKnZR16BubSsYPfD7Ujr/lBGPubOW65wMS7IW8lEzGL0JgQy/hfr8SzBtBUSSkImA1wBCQz1XgRGDQsl/Qr2jFb1y6niYNoaVBUiuVMUPSPhi3UhSnrtktgfKuBgym9X3xJkqK9VSZwmZv1CBGDgz1Wi/JWl2tXrwFLcHQX4WL4PBaT0yVNGtwtxHLAa/bzxSjdJ8zh7iD/7geTN/j4Zq7KxLvJdEOKxd5UYhHMQ2rQ+1izit/JhBsVkiKF6P5fLhhCJK5g/6zdrQmngSQ+b8HljvzdCS8aljTc5xK1/FbhT5pBvXRvYa7rAghxR5u/BYsboAxtcQw+nTRAhjaLWUFO0p0BBcv8XKs5plyk+RhZbDaCXySIEvbAt5IOV47jvC1QNp2e/RKsgGt0hlwu3xdXMFvj1ybMC3enH5KDgl1NQCjWlaxEvMtxv875/QNQ7X8QaetYWVOAo0fX5jRRLIrFmpuKx+SO++zQsqBjVr2d10nEQb74x9HwcqiRzagyUEoWsHlbcoq69ib8CHl38MiDybwmKTY3/SnK;");
        HttpResponse response = httpClient.execute(getMethod);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        System.out.println(responseString);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(responseString);
        JsonNode results = actualObj.get("Results");
        if (results.isArray()) {
            for (final JsonNode objNode : results) {
                System.out.println(objNode.get("docTitle"));
            }
        }

        System.out.println(response.getStatusLine());
	}

}
