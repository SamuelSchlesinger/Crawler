import java.util.Queue;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Connection;

public class Crawler {
    private Document DOM;
    private String URL;
    private Connection connection;
    boolean exist;
    static TreeMap<String, Integer> pastURLS = new TreeMap<String, Integer>();
    static TreeMap<String, Integer> pastStrings = new TreeMap<String, Integer>();
    static int N = 0;

    public Crawler(String url) {
        //System.out.println("Trying " + url);
        try {
            // Create a new connection to the specified URL
            this.connection = Jsoup.connect(url)
                                   .userAgent("Mozilla/5.0 (X11; Linuxi686; rv:10.0) Gecko/20100101 Firefox/10.0")
                                   .referrer("http://www.google.com");
            // Extract the DOM from the connection
            this.DOM = connection.get();
            // Save the URL string
            this.URL = url;
            //System.out.println("Title: " + DOM.title());
            //System.out.println("success");
            this.exist = true;
            N++;
        } catch (Exception e) {
            exist = false;    
        }
    }

    // Breadth first creep through the page
    public void creep(int n, String criteria) {
        Elements links = DOM.select("a[href]");
        Queue<Crawler> queue = new LinkedList<Crawler>();
        for (Element E: links) {
             String href = E.attr("href");
                    if (!pastURLS.containsKey(href)) {
                        queue.offer(new Crawler(href));
                        pastURLS.put(href, 1);
                    }
        }
        while (n > 0) {
            Crawler current = queue.poll();
            if (current != null && current.exist == true) {
                n--;
                links = current.DOM.select("a[href]");
                for (Element E: links) {
                    String href = E.attr("href");
                    if (href.charAt(0) == '/') {
                        href = current.URL.substring(0, current.URL.length() - 1) + href;
                    }
                    if (!href.contains("/")) {}
                    else if (!pastURLS.containsKey(href)) {
                        Crawler new_crawler = new Crawler(href);
                        queue.offer(new_crawler);
                        pastURLS.put(href, 1);
                        //System.out.print('.');
                        if (new_crawler.exist)
                            new_crawler.search(criteria);
                    } else {
                        pastURLS.put(href, pastURLS.get(href) + 1);
                    }
                } 
            }
        }
    }

    public void search(String search) {
        Elements paragraphs = DOM.select("*");
        for (Element E: paragraphs) {
            String text = E.ownText();
            if (text.contains(search)) {
                if (!pastStrings.containsKey(text)) {
                    System.out.println("Found: " + text + String.format(" (%s)", URL));
                    pastStrings.put(text, 1);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            Crawler crawler = new Crawler(args[0]);
            crawler.creep(Integer.parseInt(args[2]), args[1]);
            Set<String> urls = pastURLS.keySet();
            System.out.println("Number of URLs Visited: " + N);
            for (String u: urls) {
                System.out.println(u + ": " + pastURLS.get(u));
            } 
        } catch (Exception e) {
            System.out.println("No internet or something.");
        }
    }
}
