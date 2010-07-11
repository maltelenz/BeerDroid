require 'open-uri'
require 'rubygems'
require 'hpricot'
class HomeController < ApplicationController
  def index
    
  end
  
  def get_ba_by_id
    url = "http://beeradvocate.com/beer/profile/" + params[:brewery] + "/" + params[:id]
    page_content = open(url).read
    page = Hpricot(page_content)
    @beer_name = page.at("h1").inner_html
    @rating = page.at("//td[@style='background:#FFFFFF;']/span[@class='BAscore_big']").inner_html
    @style = page.at("//a[@href^='/beer/style/']/b").inner_html
    @abv_element = page.at("//a[@href='/articles/518']").previous.inner_text
    @abv = @abv_element[5..-3]
    respond_to do |format|
      format.html {
        render :json => {
          :beer_name => @beer_name,
          :rating => @rating,
          :style => @style,
          :abv => @abv
        }
      }
    end
  end
end
