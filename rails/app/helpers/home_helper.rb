require 'iconv'
module HomeHelper
  
  #fetches 'nr_results' beeradvocate.com id's from google
  #result is formatted in the style:
  # [{:brewery => breweryid, :beer => beerid},
  #  ...]
  #
  def fetch_ba_id_google(query,nr_results = 1)
    url = "http://ajax.googleapis.com/ajax/services/search/web" + \
    "?v=1.0&rsz=8&q=site:http://beeradvocate.com/beer/profile/%20" + URI.escape(query)
    page_stream = open(url)
    page_content = Iconv.conv("utf-8",page_stream.charset,page_stream.read)

    result_hash = JSON.parse(page_content)
    #collect an array of the ids
    results = (0..Integer(nr_results)-1).collect{
      |i|
      begin
        #split the url up to get the id at the end (after 'profile')
        spliturl = result_hash["responseData"]["results"][i]["url"].split("/")
        start = spliturl.index("profile") + 1
        brewery = spliturl[start]
        beer = spliturl[start+1]
        {:brewery => brewery ,:beer => beer}
      rescue NoMethodError
        #failed to get an id
        nil
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
    page_stream = open(url)
    page_content = Iconv.conv("utf-8",page_stream.charset,page_stream.read)

    page = Hpricot(page_content)
    begin
      #sort out the values we want
      beer_name = page.at("h1").inner_html
      rating = page.at("//td[@style='background:#FFFFFF;']/span[@class='BAscore_big']").inner_html
      style = page.at("//a[@href^='/beer/style/']/b").inner_html
      brewery_name = page.at("//a[@href^='/beer/profile/']/b").inner_html
      abv_element = page.at("//a[@href='/articles/518']").previous.inner_text
      #sort of an ugly hack. this interval is the percentage.
      abv = abv_element[5..-3]
    rescue NoMethodError
      #no hit
      return nil
    end
    return {
      :beer_name => beer_name,
      :rating => rating,
      :style => style,
      :abv => abv,
      :brewery_name => brewery_name
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
  def fetch_systemet_info_by_name(query, options = {})
    #Default arguments
    options.reverse_merge! :nr_results => 1, :county => nil
    #fetch keyword arguments
    nr_results = options[:nr_results]
    county = options[:county]

    url = "http://agent.nocrew.org/xml/ws/search/?query=" + CGI::escape(query)
    page_stream = open(url)
    page_content = Iconv.conv("utf-8",page_stream.charset,page_stream.read)

    page = Hpricot(page_content)
    
    #collect an array of nicely formatted results
    results = (1..Integer(nr_results)).collect{
      |i|
      #get the product
      product = page.at("//product[@count='" + i.to_s + "']")
      if product
        #sort out all the wanted values
        beer_name = product.at("/name").inner_html
        systemet_id = product.at("/id[@key='systembolaget']").inner_html
        price = product.at("/price[@currency='SEK'").inner_html
        size = product.at("/size[@measure='ml']").inner_html
        #protect for the possibility there is no link to beeradvocate
        begin
          ba_url = product.at("/urls/url[@source='BeerAdvocate']").inner_html
        rescue NoMethodError
          #no ba link
        end
        if ba_url
          #sort out the beeradvocate id from the link
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
        systemet_available = []

        #if a county is given, add availability info as well
        if county != nil
          systemet_available = fetch_systemet_availability(systemet_id, county)
        end
        #add the completed object to the array
        {
          :beer_name => beer_name,
          :systemet_id => systemet_id,
          :price => price,
          :size => size,
          :ba_id => ba_id,
          :systemet_available => systemet_available
        }
      else
        #no more hits
        nil
      end
    }
    #return the array, unique and non-nil entries only
    return results.uniq.compact
  end
  
  def fetch_systemet_availability(systemet_id, county)
    url = "http://agent.nocrew.org/xml/ws/inventory/?sysid=" + CGI::escape(systemet_id)
    page_stream = open(url)
    page_content = Iconv.conv("utf-8",page_stream.charset,page_stream.read)

    page = Hpricot(page_content)
    
    results = page.search("//product/stores/items[@county='" + county + "']").collect{
      |entry|
      {
        :store => entry[:address],
        :nr => entry.inner_html
      }
    }
    #return the array, unique and non-nil entries only
    return results.uniq.compact
  end

  #takes a string query and fetches combined info from
  # beeradvocate.com and systembolaget (via agent.nocrew.org)
  #return is in format:
  #  [{:beer_name => @beer_name,
  #    :rating => @rating,
  #    :style => @style,
  #    :abv => @abv,
  #    :ba_id => {:brewery => brewery_id ,:beer => beer_id},
  #    :systemet_id => systembolaget_id,
  #    :systemet_price => systembolaget_price, #in SEK
  #    :systemet_size => systembolaget_unit_size #in ml
  #  },
  #  ...]
  # 
  def fetch_all_possible_info(query, options = {})
    #Default arguments
    options.reverse_merge! :county => nil
    #fetch keyword arguments
    county = options[:county]
    
    #get up to 20 systemet entries
    systemet_infos = fetch_systemet_info_by_name(query, {:nr_results => 20, :county => county})
    #and up to 10 beeradvocate.com ids from google
    ba_ids = fetch_ba_id_google(params[:query],10)
    #join the two result sets
    final_result = ba_ids.collect{
      |ba_id_iter|
      if ba_id_iter
        #get info from beeradvocate.com
        beer_info = fetch_ba_info(ba_id_iter[:brewery],ba_id_iter[:beer])
        if !beer_info
          #wrong beeradvocate.com id
          next
        end
        beer_info[:ba_id] = ba_id_iter
        #check if systembolaget has it
        systemet_infos.each do |sys_info|
          if sys_info[:ba_id] == ba_id_iter
            beer_info[:systemet_id] = sys_info[:systemet_id]
            beer_info[:systemet_price] = sys_info[:price]
            beer_info[:systemet_size] = sys_info[:size]
   
            if sys_info[:systemet_available]
              beer_info[:systemet_available] = sys_info[:systemet_available]
            end
            #we found one, so we can stop looking
            break
          end
        end
        beer_info #One piece of final result
      else
        nil #we are past the real hits
      end
    }
    #return only unique hits and remove nils
    return final_result.uniq.compact
  end
  
  #If the given object can be seen as a number
  def is_numeric?(i)
    i.to_i.to_s == i
  end



end
