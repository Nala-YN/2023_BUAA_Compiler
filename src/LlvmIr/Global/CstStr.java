package LlvmIr.Global;

import LlvmIr.Type.ArrayType;
import LlvmIr.Type.LlvmType;
import LlvmIr.Type.PointerType;
import LlvmIr.Value;

public class CstStr extends Value {
    private String content;

    public CstStr(String name, String content) {//初始化时未在末尾\0;
        super(name, new PointerType(new ArrayType(content.length()+1,LlvmType.Int8)));
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String toString() {
        return name + " = constant " + ((PointerType)type).getPointedType()+ " c\"" + content + "\\00\"";
    }
}
