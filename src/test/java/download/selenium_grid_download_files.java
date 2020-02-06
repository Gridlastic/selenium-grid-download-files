package download;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import junit.framework.TestCase;

public class selenium_grid_download_files extends TestCase {
	 private RemoteWebDriver driver;

	 //NOTE: find these credentials in your Gridlastic dashboard after launching your selenium grid (get a free account).
	 String video_url = System.getenv("VIDEO_URL");
	 String hub = System.getenv("HUB"); // like "http://USERNAME:ACCESS_KEY@SUBDOMAIN.gridlastic.com/wd/hub";

	 
	 public void setUp() throws Exception {
	     
	 	ChromeOptions options = new ChromeOptions();
	 	options.setCapability("version", "latest");
	 	options.setCapability("platform", Platform.WIN10);
	 	options.setCapability("platformName", "windows");
	 	options.setCapability("video", "True");
	 		 	
	 	
	 	HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("download.prompt_for_download", Boolean.valueOf(false));
        chromePrefs.put("plugins.always_open_pdf_externally", Boolean.valueOf(true));
        chromePrefs.put("safebrowsing_for_trusted_sources_enabled", Boolean.valueOf(false));
        options.setExperimentalOption("prefs", chromePrefs);
        
    
	     driver = new RemoteWebDriver(
	        new URL(hub),
	        options);
	     driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	     System.out.println("GRIDLASTIC VIDEO URL: " + video_url + ((RemoteWebDriver) driver).getSessionId()); 
	 }

	 public void test_download_file() throws Exception {
//			File download_url = new File((String) "http://ipv4.download.thinkbroadband.com/50MB.zip");	
			File download_url = new File((String) "https://static.mozilla.com/foundation/documents/mf-articles-of-incorporation.pdf");		
			driver.get(download_url.toString());
			
			  int count = 1;
		        do {
		        	if (get_downloaded_files((RemoteWebDriver) driver).toString().contains((download_url.getName().substring(0,download_url.getName().indexOf(".")-1)))){ //Note: multiple file downloads on the same grid node of the same file name will increment the file name like 50MB(2).zip 
		        		System.out.println("FILE DOWNLOADED TO GRID NODE");
		    			break;
		    		} else {
		    			System.out.println("DOWNLOAD PROGRESS: " + get_download_progress_all((RemoteWebDriver) driver));	
		    		}
		             count++;
		             Thread.sleep(5000);
		        } while (count < 11);
		        
		        
		        ArrayList downloaded_files_arraylist = get_downloaded_files((RemoteWebDriver) driver);
		    	String content = get_file_content((RemoteWebDriver) driver,(String) downloaded_files_arraylist.get(0));// large files might need and increase in implicit wait.
		    	 try {	
		    		 	String home = System.getProperty("user.home");
		    	    	FileOutputStream fos = new FileOutputStream(home+"/downloads/gridnodes/" + download_url .getName());		    	    	
		    	        byte[] decoder = Base64.decodeBase64(content.substring(content.indexOf("base64,")+7));
		    	        fos.write(decoder);
		    	        System.out.println("File saved to local.");
		    	      } catch (Exception e) {
		    	        e.printStackTrace();
		    	      }
	}

	 public void tearDown() throws Exception {
	     driver.quit();
	 }
	 
	 
	 private static String get_file_content(RemoteWebDriver remoteDriver,String path) {
			String file_content = null;
				try {
					if(!remoteDriver.getCurrentUrl().startsWith("chrome://downloads")) {
					remoteDriver.get("chrome://downloads/");
					}


				    WebElement elem = (WebElement) remoteDriver.executeScript(
						    "var input = window.document.createElement('INPUT'); "+
						    "input.setAttribute('type', 'file'); "+
						    "input.hidden = true; "+
						    "input.onchange = function (e) { e.stopPropagation() }; "+
						    "return window.document.documentElement.appendChild(input); "
						    ,"" );
					
					 elem.sendKeys(path);
				
				 file_content = (String) remoteDriver.executeAsyncScript(
							    "var input = arguments[0], callback = arguments[1]; "+
							    "var reader = new FileReader(); "+
							    "reader.onload = function (ev) { callback(reader.result) }; "+
							    "reader.onerror = function (ex) { callback(ex.message) }; "+
							    "reader.readAsDataURL(input.files[0]); "+
							    "input.remove(); "
							    , elem);
					
					if (!file_content.startsWith("data:")){
						System.out.println("Failed to get file content");
					}
				
				} catch (Exception e) {
					System.err.println(e);
				}
return file_content;

			}
	
		 
		 
		 private static ArrayList get_downloaded_files(RemoteWebDriver remoteDriver) {
			 ArrayList filesFound = null;
				try {
					if(!remoteDriver.getCurrentUrl().startsWith("chrome://downloads")) {
					remoteDriver.get("chrome://downloads/");
					}
					filesFound =  (ArrayList)  remoteDriver.executeScript("return downloads.Manager.get().items_   "
						   + "  .filter(e => e.state === 'COMPLETE')  "
						  +  "  .map(e => e.filePath || e.file_path); ", "");
				} catch (Exception e) {
					System.err.println(e);
				}
				return filesFound;
			}
		 
		 private static String get_download_progress(RemoteWebDriver remoteDriver) {
			 String progress = null;
				try {
					if(!remoteDriver.getCurrentUrl().startsWith("chrome://downloads")) {
					remoteDriver.get("chrome://downloads/");
					}
					progress=  (String) remoteDriver.executeScript(						
							"var tag = document.querySelector('downloads-manager').shadowRoot;"+
						    "var intag = tag.querySelector('downloads-item').shadowRoot;"+
						    "var progress_tag = intag.getElementById('progress');"+
						    "var progress = null;"+
						   " if(progress_tag) { "+
						    "    progress = progress_tag.value; "+
						  "  }" +
						    "return progress;"
							,"");
					
		
				} catch (Exception e) {
					System.err.println(e);
				}
				return progress;
			}

		 
		 
		 
		 private static ArrayList get_download_progress_all(RemoteWebDriver remoteDriver) {
			 ArrayList progress = null;
				try {
					if(!remoteDriver.getCurrentUrl().startsWith("chrome://downloads")) {
					remoteDriver.get("chrome://downloads/");
					}
					progress=  (ArrayList) remoteDriver.executeScript(						
							" var tag = document.querySelector('downloads-manager').shadowRoot;" + 
							"			    var item_tags = tag.querySelectorAll('downloads-item');" + 
							"			    var item_tags_length = item_tags.length;" + 
							"			    var progress_lst = [];" + 
							"			    for(var i=0; i<item_tags_length; i++) {" + 
							"			        var intag = item_tags[i].shadowRoot;" + 
							"			        var progress_tag = intag.getElementById('progress');" + 
							"			        var progress = null;" + 
							"			        if(progress_tag) {" + 
							"			            var progress = progress_tag.value;" + 
							"			        }" + 
							"			        progress_lst.push(progress);" + 
							"			    }" + 
							"			    return progress_lst",
							"");
					
		
				} catch (Exception e) {
					System.err.println(e);
				}
				return progress;
			}	
		
	 
	}


