require 'open-uri'
require 'rubygems'
require 'hpricot'
require 'json'

class HomeController < ApplicationController
  include HomeHelper

  def index
    
  end

  #return json of ba id of query
  # indata is :query for the name and
  #  :nr for the number of results
  #returns json in the format:
  # [{:brewery => breweryid, :beer => beerid},
  #  ...]
  # padded with nils up to :nr
  def find_ba_id_google
    nr_results = 1
    if params[:nr]
      nr_results = params[:nr]
    end
    results = fetch_ba_id_google(params[:query],nr_results)
    render :json => results
  end

  #returns json with info about a beer based on a name
  # indata is a name
  #returns json in the format:
  #  {:beer_name => @beer_name,
  #    :rating => @rating,
  #    :style => @style,
  #    :abv => @abv
  #  }
  #
  def get_ba_by_name
    ba_id = fetch_ba_id_google(params[:query],1)[0]
    if not ba_id
      render :json => { :result => "No beers found" }
    else
      bainfo = fetch_ba_info(ba_id[:brewery],ba_id[:beer])
      render :json => bainfo
    end
  end

  #returns beer info if given a beeradvocate id
  #return is in format:
  #  {:beer_name => @beer_name,
  #    :rating => @rating,
  #    :style => @style,
  #    :abv => @abv
  #  }
  #
  def get_ba_by_id
    render :json => fetch_ba_info(params[:brewery],params[:id])
  end



  #returns beer info from systemet given a query
  #return is in format:
  #  {
  #    :beer_name => beer_name,
  #    :systemet_id => systemet_id,
  #    :price => price_SEK,
  #    :size => size_ml,
  #    :ba_id => {:brewery => brewery_id ,:beer => beer_id}
  #  }
  #
  def get_systemet_by_name
    render :json => fetch_systemet_info_by_name(params[:query])
  end

  def super_search
    render :json => fetch_all_possible_info(params[:query])
  end

end
