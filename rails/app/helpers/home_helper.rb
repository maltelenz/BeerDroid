module HomeHelper
  
  #fetches 'nr_results' beeradvocate.com id's from google
  #result is formatted in the style:
  # [{:brewery => breweryid, :beer => beerid},
  #  ...]
  #
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
    return results.uniq.compact
  end

  #Fetches beer info from beeradvocate.com with a given id
  # in data is a brewery id and a beer id
  # returns a collection with syntax:
  #  {:beer_name => @beer_name,
  #    :rating => @rating,
  #    :style => @style,
  #    :abv => @abv
  #  }
  #
  def fetch_ba_info(brewery,beer)
    url = "http://beeradvocate.com/beer/profile/" +  CGI::escape(brewery.to_s) + "/" +  CGI::escape(beer.to_s)
    page_content = open(url).read
    page = Hpricot(page_content)
    begin
      @beer_name = page.at("h1").inner_html
      @rating = page.at("//td[@style='background:#FFFFFF;']/span[@class='BAscore_big']").inner_html
      @style = page.at("//a[@href^='/beer/style/']/b").inner_html
      @abv_element = page.at("//a[@href='/articles/518']").previous.inner_text
      @abv = @abv_element[5..-3]
    rescue NoMethodError
      #no hit
      return nil
    end
    return {
      :beer_name => @beer_name,
      :rating => @rating,
      :style => @style,
      :abv => @abv
    }
  end

  #fetches systembolaget info from agent.nocrew.org
  #returns an array of this type of items:
  #  {
  #    :beer_name => beer_name,
  #    :systemet_id => systemet_id,
  #    :price => price_SEK,
  #    :size => size_ml,
  #    :ba_id => {:brewery => brewery_id ,:beer => beer_id}
  #  }
  #
  def fetch_systemet_info_by_name(query,nr_results=1)
    url = "http://agent.nocrew.org/xml/ws/search/?query=" + CGI::escape(query)
    page_content = open(url).read
    page = Hpricot(page_content)
    
    results = (0..Integer(nr_results)-1).collect{
      |i|
      product = page.at("//product[@count='" + i.to_s + "']")
      if product
        beer_name = product.at("/name").inner_html
        systemet_id = product.at("/id[@key='systembolaget']").inner_html
        price = product.at("/price[@currency='SEK'").inner_html
        size = product.at("/size[@measure='ml']").inner_html
        begin
          ba_url = product.at("/urls/url[@source='BeerAdvocate']").inner_html
        rescue NoMethodError
          #no ba link
        end
        if ba_url
          begin
            spliturl = ba_url.split("/")
            start = spliturl.index("profile") + 1
            brewery = spliturl[start]
            beer = spliturl[start+1]
            ba_id = {:brewery => brewery ,:beer => beer}
          rescue NoMethodError
            ba_id = nil
          end
        end
        {
          :beer_name => beer_name,
          :systemet_id => systemet_id,
          :price => price,
          :size => size,
          :ba_id => ba_id
        }
      else
        nil
      end
    }
    return results.uniq.compact
  end
  
  def fetch_all_possible_info(query)
    systemet_infos = fetch_systemet_info_by_name(query,20)
    ba_ids = fetch_ba_id_google(params[:query],10)
    final_result = ba_ids.collect{
      |ba_id_iter|
      if ba_id_iter
        beer_info = fetch_ba_info(ba_id_iter[:brewery],ba_id_iter[:beer])
        if !beer_info
          next
        end
        beer_info[:ba_id] = ba_id_iter
        #check if systembolaget has it
        systemet_infos.each do |sys_info|
          if sys_info[:ba_id] == ba_id_iter
            beer_info[:systemet_id] = sys_info[:systemet_id]
            beer_info[:systemet_price] = sys_info[:price]
            beer_info[:systemet_size] = sys_info[:size]
            #we found one, so we can stop looking
            break
          end
        end
        beer_info #One piece of final result
      else
        nil #we are past the real hits
      end
    }
    return final_result.uniq.compact
  end
  
  #If the given object can be seen as a number
  def is_numeric?(i)
    i.to_i.to_s == i
  end



end
