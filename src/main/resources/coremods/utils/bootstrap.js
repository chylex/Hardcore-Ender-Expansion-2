var api = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var op = Java.type("org.objectweb.asm.Opcodes");

api.loadFile("coremods/utils/constants.js");
api.loadFile("coremods/utils/helpers.js");

var classTransformer = function(className, callback){
    return {
        target: {
            "type": "CLASS",
            "name": className
        },
        transformer: function(cls){
            var result = callback(cls);

            if (result == null){
                throw "Failed applying HEE2 transformer to class " + className;
            }

            api.log("INFO", "Successfully patched " + className);
            return result;
        }
    };
};

var methodTransformer = function(className, methodName, methodDesc, callback){
    var fullName = className + "." + methodName + methodDesc;

    return {
        target: {
            "type": "METHOD",
            "class": className,
            "methodName": methodName,
            "methodDesc": methodDesc
        },
        transformer: function(method){
            var instructions = method.instructions;
            var result = callback(method, instructions);

            if (result == null){
                printInstructions(instructions);
                throw "Failed applying HEE2 transformer to method " + fullName;
            }

            api.log("INFO", "Successfully patched " + fullName);
            return result;
        }
    };
};
