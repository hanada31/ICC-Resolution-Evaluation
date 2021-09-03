"use strict";

jQuery.fn.extend({
    // Auto-Height for textarea
    autoHeight: function(){
        return this.each(function(){
            const $this = jQuery(this);
            if( !$this.attr('_initAdjustHeight') ){
                $this.attr('_initAdjustHeight', $this.outerHeight());
            }
            $this.on('input', function(){
                _adjustH(this);
            });
        });
        function _adjustH(elem){
            const $obj = jQuery(elem);
            return $obj.css({height: $obj.attr('_initAdjustHeight'), 'overflow-y': 'hidden'})
                .height( elem.scrollHeight );
        }
    }
});

String.prototype.format = function(args) {
    let reg;
    if (arguments.length > 0) {
        let result = this;
        if (arguments.length === 1 && typeof(args) == "object") {
            for (const key in args) {
                if (!args.hasOwnProperty(key)) continue;
                reg = new RegExp("({" + key + "})", "g");
                result = result.replace(reg, args[key]);
            }
        } else {
            for (let i = 0; i < arguments.length; i++) {
                if (arguments[i] === undefined) {
                    return "";
                } else {
                    reg = new RegExp("({[" + i + "]})", "g");
                    result = result.replace(reg, arguments[i]);
                }
            }
        }
        return result;
    } else {
        return this;
    }
};

const HTMLDecode = function(text) {
    let temp = document.createElement("div");
    temp.innerHTML = text;
    const output = temp.innerText || temp.textContent;
    temp = null;
    return output;
};
