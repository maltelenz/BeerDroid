module HomeHelper
  
  def fetch_ba_id_google(query,nr_results = 1)
    url = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=site:http://beeradvocate.com/beer/profile/%20" + URI.escape(query)
    page_content = open(url).read
    result_hash = JSON.parse(page_content)
    results = (0..Integer(nr_results)-1).collect{
      |i|
      begin
        spliturl = result_hash["responseData"]["results"][i]["url"].split("/")
        start = spliturl.index("profile") + 1
        brewery = spliturl[start]
        beer = spliturl[start+1]
        {:brewery => brewery ,:beer => beer}
      rescue NoMethodError
        
      end
    }
    #If you just JSON this later, it seems to become randomized order
    return results
  end

  def fetch_ba_info(brewery,beer)
    url = "http://beeradvocate.com/beer/profile/" + brewery.to_s + "/" + beer.to_s
    page_content = open(url).read
    page = Hpricot(page_content)
    @beer_name = page.at("h1").inner_html
    @rating = page.at("//td[@style='background:#FFFFFF;']/span[@class='BAscore_big']").inner_html
    @style = page.at("//a[@href^='/beer/style/']/b").inner_html
    @abv_element = page.at("//a[@href='/articles/518']").previous.inner_text
    @abv = @abv_element[5..-3]
    return {
      :beer_name => @beer_name,
      :rating => @rating,
      :style => @style,
      :abv => @abv
    }
  end

  def is_numeric?(i)
    i.to_i.to_s == i
  end
end
