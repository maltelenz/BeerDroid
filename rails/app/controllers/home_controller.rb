require 'open-uri'
require 'rubygems'
require 'hpricot'
require 'json'

class HomeController < ApplicationController
  include HomeHelper

  def index
    
  end

  def find_ba_id_google
    nr_results = 1
    if params[:nr]
      nr_results = params[:nr]
    end
    results = fetch_ba_id_google(params[:query],nr_results)
    render :json => results
  end



  def get_ba_by_name
    ba_id = fetch_ba_id_google(params[:query],1)[0]
    if not ba_id
      render :json => { :result => "No beers found" }
    else
      bainfo = fetch_ba_info(ba_id[:brewery],ba_id[:beer])
      render :json => bainfo
    end
  end

  def get_ba_by_id
    render :json => fetch_ba_info(params[:brewery],params[:id])
  end
end
