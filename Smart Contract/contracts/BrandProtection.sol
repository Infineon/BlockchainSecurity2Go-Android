pragma solidity ^0.4.24;

/**
 * This is a DEMO contract. Please look carefully into the source code
 * before using any of this in production.
 * 
 * This contract helps upload a product details
 * 
 * */
contract Product{
    
    struct ProductDetail{
        
        string productID;
        string productName;
        string time;
        string date;
        string manufacturer;
        string publickey;
        
    }
    
    mapping(address=>ProductDetail) productDetails;
    
    // will hold all the product address
    address[] public productAccts;

    function setProductDetail(address _address, string _productID, string _productName, string _time, string _date, string _manufacturer, string _publickey)   public {
        var product=productDetails[_address];
      
        require( checkProductAddress(_address)== false,"address must be unique");
        
        product.productID=_productID;
        product.productName=_productName;
        product.time=_time;
        product.date=_date;
        product.manufacturer=_manufacturer;
        product.publickey=_publickey;
        
        
        productAccts.push(_address)-1;
        
    }
    
    // to get product details stored using the address
    
    function getProductDetails(address _address) view public returns(string, string, string, string, string) {
        
            if(checkProductAddress(_address)==true){
                
            
            return(productDetails[_address].productID, productDetails[_address].productName, productDetails[_address].time, productDetails[_address].date, productDetails[_address].manufacturer);
            
            
          }
        
    }
    
    // checks for the address, if address already present cannot upload the product data
    
    function checkProductAddress(address _address) view public returns(bool){
        
             for(uint i=0; i<productAccts.length; i++){
            
            if(productAccts[i]== _address){
                
                    return bool(true);
                    
               }
             }
                 return bool(false);
            
    }
}

