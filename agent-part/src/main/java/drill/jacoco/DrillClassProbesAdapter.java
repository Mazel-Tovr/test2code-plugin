package drill.jacoco;

import org.jacoco.core.internal.flow.*;
import org.jacoco.core.internal.instr.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

import java.util.*;

public class DrillClassProbesAdapter extends ClassVisitor
        implements IProbeIdGenerator {

    private static final MethodProbesVisitor EMPTY_METHOD_PROBES_VISITOR = new MethodProbesVisitor() {
    };

    private final ClassProbesVisitor cv;

    private final boolean trackFrames;

    private int counter = 0;

    private String name;

    public HashMap<String, Integer> methodProbes = new HashMap<>();

    private String currentMethodUUID;

    /**
     * Creates a new adapter that delegates to the given visitor.
     *
     * @param cv          instance to delegate to
     * @param trackFrames if <code>true</code> stackmap frames are tracked and provided
     */
    public DrillClassProbesAdapter(final ClassProbesVisitor cv,
                                   final boolean trackFrames) {
        super(InstrSupport.ASM_API_VERSION, cv);
        this.cv = cv;
        this.trackFrames = trackFrames;
    }

    @Override
    public void visit(final int version, final int access, final String name,
                      final String signature, final String superName,
                      final String[] interfaces) {
        this.name = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public final MethodVisitor visitMethod(final int access, final String name,
                                           final String desc, final String signature,
                                           final String[] exceptions) {
        final MethodProbesVisitor methodProbes;
        final MethodProbesVisitor mv = cv.visitMethod(access, name, desc,
                signature, exceptions);
        if (mv == null) {
            // We need to visit the method in any case, otherwise probe ids
            // are not reproducible
            methodProbes = EMPTY_METHOD_PROBES_VISITOR;
        } else {
            methodProbes = mv;
        }
        return new DrillMethodSanitizer(null, access, name, desc, signature,
                exceptions) {

            @Override
            public void visitEnd() {
                super.visitEnd();
                LabelFlowAnalyzer.markLabels(this);
                DrillClassProbesAdapter.this.currentMethodUUID = this.methodUUID;
                DrillClassProbesAdapter.this.methodProbes.put(currentMethodUUID, 0);
                DrillClassProbesAdapter.this.counter = 0;
                final MethodProbesAdapter probesAdapter = new MethodProbesAdapter(
                        methodProbes, DrillClassProbesAdapter.this);
                if (trackFrames) {
                    final AnalyzerAdapter analyzer = new AnalyzerAdapter(
                            DrillClassProbesAdapter.this.name, access, name, desc,
                            probesAdapter);
                    probesAdapter.setAnalyzer(analyzer);
                    methodProbes.accept(this, analyzer);
                } else {
                    methodProbes.accept(this, probesAdapter);
                }
            }
        };
    }

    @Override
    public void visitEnd() {
        cv.visitTotalProbeCount(counter);
        super.visitEnd();
    }

    // === IProbeIdGenerator ===

    public int nextId() {
        methodProbes.put(currentMethodUUID, methodProbes.get(currentMethodUUID) + 1);
        return counter++;
    }

}
