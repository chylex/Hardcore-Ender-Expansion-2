var makeInstructions = function(callback){
    var helper = api.getMethodNode();
    callback(helper);
    return helper.instructions;
};

var getSkipInst = function(label){
    var labelInst = label.getLabel();
    labelInst.info = label;
    return labelInst;
};

var printInstructions = function(instructions){
    var appendSafe = function(title, getter){
        try{
            var item = getter();
            
            if (item){
                title += ", " + item;
            }
        }catch(e){}

        return title;
    };
    
    for(var index = 0, count = instructions.size(); index < count; index++){
        var instruction = instructions.get(index);
        
        var indexStr = index + ": ";
        var typeName = getInstructionTypeName(instruction);
        var opcodeName = getInstructionOpcodeName(instruction);
        
        while(indexStr.length() < 6){
            indexStr = " " + indexStr;
        }
        
        while(typeName.length() < 12){
            typeName = typeName + " ";
        }
        
        if (opcodeName.length() > 0){
            opcodeName = " | " + opcodeName;
            opcodeName = appendSafe(opcodeName, function(){ return instruction.name; });
            opcodeName = appendSafe(opcodeName, function(){ return instruction.desc; });
            opcodeName = appendSafe(opcodeName, function(){ return instruction.var; });
            opcodeName = appendSafe(opcodeName, function(){
                var label = instruction.label;
                
                if (label){
                    for(var search = 0; search < count; search++){
                        if (instructions.get(search) == label){
                            return search;
                        }
                    }
                }
                
                return null;
            });
        }
        
        print(indexStr + typeName + opcodeName);
    }
};
