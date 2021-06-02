package drill.jacoco;

import org.jacoco.core.internal.instr.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public class DrillMethodSanitizer extends JSRInlinerAdapter {

    public String methodUUID;

    public DrillMethodSanitizer(final MethodVisitor mv, final int access, final String name,
                                final String desc, final String signature,
                                final String[] exceptions) {
        super(InstrSupport.ASM_API_VERSION, mv, access, name, desc, signature,
                exceptions);
        this.methodUUID = name + desc;
    }

    @Override
    public void visitLocalVariable(final String name, final String desc,
                                   final String signature, final Label start, final Label end,
                                   final int index) {
        // Here we rely on the usage of the info fields by the tree API. If the
        // labels have been properly used before the info field contains a
        // reference to the LabelNode, otherwise null.
        if (start.info != null && end.info != null) {
            super.visitLocalVariable(name, desc, signature, start, end, index);
        }
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
        // Here we rely on the usage of the info fields by the tree API. If the
        // labels have been properly used before the info field contains a
        // reference to the LabelNode, otherwise null.
        if (start.info != null) {
            super.visitLineNumber(line, start);
        }
    }

}
