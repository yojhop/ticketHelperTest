function addToCart(goodsId, parentId)
{
  var goods        = new Object();
  var spec_arr     = new Array();
  var fittings_arr = new Array();
  var number       = 1;
  var quick		   = 0;

  // 检查是否有商品规格 

  var spec_arr = new Array();
  
  number=1;
  goods.quick    = quick;
  goods.spec     = spec_arr;
  goods.goods_id = goodsId;
  goods.number   = number;
  goods.parent   = (typeof(parentId) == "undefined") ? 0 : parseInt(parentId);
  Ajax.call('flow.php?step=add_to_cart', 'goods=' + goods.toJSONString(), addToCartResponse, 'POST', 'JSON');
}