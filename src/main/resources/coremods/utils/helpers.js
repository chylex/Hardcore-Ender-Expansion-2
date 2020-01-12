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

            try{
                var name = instruction.name;

                if (name){
                    opcodeName += ", " + name;
                }
            }catch(e){}

            try{
                var desc = instruction.desc;

                if (desc){
                    opcodeName += ", " + desc;
                }
            }catch(e){}

            try{
                var label = instruction.label;

                if (label){
                    for(var search = 0; search < instrcount; search++){
                        if (instructions.get(search) == label){
                            opcodeName += ", " + search;
                            break;
                        }
                    }
                }
            }catch(e){}
        }

        print(indexStr + typeName + opcodeName);
    }
};
