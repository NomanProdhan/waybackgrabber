/*
        Program : WaybackGrabber
        Author : Noman Prodhan
        Github : https://github.com/NomanProdhan
        Twitter : https://twitter.com/NomanProdhan
        Websites : https://nomantheking.com
                   https://knightsquad.org
                   https://hack.knightsquad.org

*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStreamReader;

public class waybackgrabber {
    public static void main(String[] args) throws IOException {
        String target;
        waybackgrabber wbl = new waybackgrabber();
        wbl.welcome_banner();
        if (args.length != 1) {
            wbl.usage(waybackgrabber.class.getSimpleName());
        } else {
            FileReader domain_list_reader = new FileReader(args[0]);
            BufferedReader bufferedReader = new BufferedReader(domain_list_reader);
            while ((target = bufferedReader.readLine()) != null) {
                if (wbl.domain_validator(target)) {
                    wbl.grab_links(target);
                } else {
                    System.out.println("Invalid domain.");
                }
            }
            bufferedReader.close();
        }
    }

    private void welcome_banner() {
        System.out.println("\t------------------------------");
        System.out.println("\t\u001B[33mWaybackGrabber by NomanProdhan \u001B[0m");
        System.out.println("\t*GitHub : @NomanProdhan");
        System.out.println("\t*Twitter : @NomanProdhan");
        System.out.println("\t------------------------------");
    }

    private void usage(String className) {
        System.out.println("\u001B[31mUsage : java " + className + " <domain_list_file>\u001B[0m\n");
    }

    private Boolean domain_validator(String domain) {
        String regex = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}";
        Pattern pattern = Pattern.compile(regex);
        if (domain.isEmpty()) {
            return false;
        } else {
            Matcher matcher = pattern.matcher(domain);
            return matcher.matches();
        }
    }

    private void grab_links(String target) {
        String target_link = "http://web.archive.org/cdx/search/cdx?url=*." + target
                + "/*&output=json&fl=original&collapse=urlkey";
        try {
            String link, host;
            int param_link_counter = 0, link_counter = 0;
            ArrayList<String> host_list = new ArrayList<>();
            host_list.add(target);
            FileWriter all_links_file = new FileWriter(target + "_links.txt", false);
            FileWriter host_file = new FileWriter(target + "_domains.txt", false);
            FileWriter param_links_file = new FileWriter(target + "_param_links.txt", false);
            URL url = new URL(target_link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            bufferedReader.readLine();
            while ((link = bufferedReader.readLine()) != null) {
                try {
                    link = link.replaceAll("[,\"\\[\\]]", "");
                    host = get_host(link);
                    if (!host_list.contains(host) && host != null) {
                        host_file.write(host + "\n");
                        host_file.flush();
                        host_list.add(host);
                    }
                    if (link.contains("=")) {
                        Boolean param_flag = false;
                        String[] extensions = exclude_extensions();
                        for (int ns = 0; ns < extensions.length; ns++) {
                            if (link.toLowerCase().contains(extensions[ns])) {
                                param_flag = false;
                            } else {
                                param_flag = true;
                            }
                        }
                        if (param_flag) {
                            param_links_file.write(link + "\n");
                            param_links_file.flush();
                            param_link_counter++;
                        }
                    }
                    all_links_file.write(link + "\n");
                    all_links_file.flush();
                    link_counter++;
                    clearScreen();
                    welcome_banner();
                    System.out.println("+ Current Target : " + target);
                    System.out.println("+ Link Found : " + link);
                    System.out.println("+ Domain Found : " + host_list.get(host_list.size() - 1));
                    System.out.println();
                    System.out.println("+ Total Links : " + link_counter);
                    System.out.println("+ Total Links with Param : " + param_link_counter);
                    System.out.println("+ Total Domains : " + (host_list.size() - 1));

                } catch (Exception e) {
                    // System.out.println(e);
                }
            }
            host_file.close();
            all_links_file.close();
            param_links_file.close();
            host_list.clear();
        } catch (MalformedURLException e) {
            System.out.println("Please check your internet connection.");
        } catch (IOException e) {
            System.out.println("Please check your internet connection.");
        }
    }

    private String get_host(String link) throws URISyntaxException {
        URI uri = new URI(link);
        return uri.getHost();
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private String[] exclude_extensions() {
        String[] extensions = { ".jpg", ".jpeg", ".png", ".js", ".gif" };
        return extensions;
    }

}
