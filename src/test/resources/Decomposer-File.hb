
float x = 1.2ff;
/**
 * 源码中的每一个部分
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 15:26
 */
public enum SourceStringType {
    MIXED,// 多种类型混合
    SIGN,// 符号, 包含运算符
    CHAR, // 源码中是常量
    NUMBER, // 整形, 包含short, integer, long
    STRING, // 源码中是字符串
    WORD, // 源码中是标识符
    SINGLE_LINE_COMMENTS, // 源码中是单行注释
    MULTI_LINE_COMMENTS // 源码中是多行注释
    "asdaksjljlaskfd
    sadkljklasjdkljas
    sadjklasjkldjkla
    asjkldjlasjk",
    MIXED,// 多种类型混合
    SIGN,// 符号, 包含运算符
    CHAR, // 源码中是常量
    NUMBER, // 整形, 包含short, integer, long
    STRING, // 源码中是字符串
    WORD, // 源码中是标识符
    SINGLE_LINE_COMMENTS, // 源码中是单行注释
    MULTI_LINE_COMMENTS // 源码中是多行注释
}

 import org.harvey.compiler.analysis.text.context.SourceTextContext;
 import org.harvey.compiler.common.entity.SourcePosition;
 import org.harvey.compiler.analysis.text.decomposer.TextDecomposerChain;
 import org.harvey.compiler.analysis.text.decomposer.CommitClearChecker;
 import org.harvey.compiler.analysis.text.decomposer.SourceFileRebuilder;
 import org.harvey.compiler.analysis.text.decomposer.StringDecomposer;
 import org.harvey.compiler.exception.CompileException;
 import org.harvey.compiler.exception.warn.CompileWarning;
 import org.harvey.compiler.io.SourceFileReader;

 import java.io.FileReader;
 import java.io.IOException;

 /**
  * 应用启动
  *
  * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
  * @version 1.0
  * @date 2024-11-17 20:08
  */
 public class Application {
     public static void main(String[] args) {
         globalExceptionHandler();
     }

     private static void globalExceptionHandler() {
         try {
             compileFile("src/test/resources/Decomposer-File");
         } catch (IOException | CompileException ce) {
             System.err.println("[ERROR] " + ce.getMessage());
         } catch (CompileWarning cw) {
             System.out.println("[WARNING] " + cw.getMessage());
         } catch (Exception e) {
             throw new RuntimeException("编译器内部异常, 建议上报[mailto:harvey.blocks@outlook.com]", e);
         }
     }

     private static void compileFile(String filename) throws Exception {
         TextDecomposerChain chain = registerChain();
         SourceTextContext context = new SourceFileReader<>(FileReader.class).read(filename);
         context = chain.execute(context);
     }

     private static TextDecomposerChain registerChain() {
         TextDecomposerChain chain = new TextDecomposerChain();
         SourcePosition sp = new SourcePosition(0, 0);
         chain.register(new CommitClearChecker())
                 .register(new SourceFileRebuilder())
                 .register(new StringDecomposer())
                 .register(source -> null);
         return chain;
     }


 }

import org.junit.Test;

import java.util.LinkedList;
import java.util.ListIterator;

public class TextDecomposerChainTest {
    @Test
    public void testIterator() {
        LinkedList<String> ll = new LinkedList<String>();
        ll.add("A");
        ll.add("B");
        ll.add("C");
        ll.add("D");
        for (ListIterator<String> it = ll.listIterator(); it.hasNext(); ) {
            String next = it.next();
            System./*123*//*123*/out./**/println(next);
            it./*123*/remove();
            it.add("X");
            it.add("Y");
            it.add("Z");
        }
        System.out.println(ll);
    }

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.PropertyConstant;
import org.harvey.compiler.common.SystemConstant;
import org.harvey.compiler.exception.analysis.AnalysisException;

import java.util.LinkedList;
import java.util.function.BiFunction;

/**
 * 文本分解器, 去除注释, 构建元素是空格分割的链表
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-15 21:21
 */
public class SimpleTextDecomposer {

    public static final String FAKE_STRING_CIRCLE_SIGN = "" + PropertyConstant.SPECIAL_CHARACTER_IDENTIFIERS + PropertyConstant.STRING_ENCIRCLE_SIGN;

    // 完成分解的文本
    private final LinkedList<SourceString> phasedText = new LinkedList<>();
    private boolean lastComplete = false;
    private final SourcePosition nowPosition = new SourcePosition(0, 0);


    static {
        assert (PropertyConstant.MULTI_LINE_COMMENTS_PRE.length() == 2);
        assert (PropertyConstant.SINGLE_LINE_COMMENTS_START.length() == 2);
    }

    /**
     * @param apartText 一部分源码文本
     */
    public void appendDecomposed(boolean fullLine, String apartText) {
        if (apartText == null || apartText.isEmpty()) {
            return;
        }
        if (fullLine && !apartText.endsWith(SystemConstant.LINE_SEPARATOR)) {
            throw new IllegalStateException("分解的字符串不对, 如果满一行, 应该有换行的字符");
        }
        SourcePosition oldPosition = updatePosition(fullLine, apartText);
        final StringBuilder initSb = new StringBuilder();
        if (!phasedText.isEmpty() && !lastComplete) {
            // 补上上次没完成的
            SourceString last = phasedText.removeLast();
            initSb.append(last.getValue());
            oldPosition = (SourcePosition) last.getPosition().clone();
        }
        StringIterator sit = new StringIterator(initSb.append(apartText).toString());
        StringBuilder sb = new StringBuilder();
        while (sit.hasNext()) {
            int index = sit.nextIndex();
            char c = sit.next();
            oldPosition.columnIncreasing();
            if (PropertyConstant.SPRITE_SIGN.contains(c)) {
                // 一定要分割的符号, 就分割
                SourcePosition endPosition = oldPosition.clone(0, -1);
                add(SourceStringType.MIXED, sb.toString(), endPosition, false);
                sb = add(SourceStringType.SIGN, String.valueOf(c), endPosition, true);
                continue;
            }

            if (c == PropertyConstant.STRING_ENCIRCLE_SIGN) {
                // 是字符串的开头
                // 处理字符串
                SourcePosition beforeString = oldPosition.clone(0, -1);
                sb = add(SourceStringType.MIXED, sb.toString(), beforeString, false);
                if ((sit = skipString(index, sit, oldPosition)) == null) {
                    break;
                } else {
                    continue;
                }
            }
            StringBuilder startSign = new StringBuilder();
            startSign.append(c);
            if (Character.isWhitespace(c)) {
                // 空白符, 就分割
                sb = add(SourceStringType.MIXED, sb.toString(), oldPosition.clone(0, -1), false);
                continue;
            }
            if (!sit.hasNext()) {
                // 不含下一个, 直接跳出
                sb.append(c);
                break;
            }
            // 怀疑有些符号是两位的, 例如多行注释是两个字符组成的
            // 能处理三个字符组成的标识吗? 四位的呢? 不能qwq
            char cn = sit.next();
            oldPosition.columnIncreasing();
            startSign.append(cn);
            String cnString = startSign.toString();
            if (cnString.equals(PropertyConstant.SINGLE_LINE_COMMENTS_START)) {
                int preLen = PropertyConstant.SINGLE_LINE_COMMENTS_START.length();
                sb = add(SourceStringType.MIXED, sb.toString(), oldPosition.clone(0, -preLen), false);
                if ((sit = skipSingleCommit(index, sit, oldPosition)) == null) {
                    break;
                }
            } else if (cnString.equals(PropertyConstant.MULTI_LINE_COMMENTS_PRE)) {
                sb = add(SourceStringType.MIXED, sb.toString(), oldPosition.clone(0, -PropertyConstant.MULTI_LINE_COMMENTS_PRE.length()), false);
                if ((sit = skipMultipleCommit(index, sit, oldPosition)) == null) {
                    break;
                }
            } else {
                sb.append(c);
                sit.previous();
                oldPosition.columnDecreasing();
            }
        }

        if (sit == null) {
            lastComplete = false;
            // 中道崩殂
            if (!sb.toString().isEmpty()) {
                throw new IllegalStateException("逻辑上不应该出现解析完注释/字符串前没有清空StringBuilder");
            }
        } else {
            // 寿终正寝
            String value = sb.toString();
            add(SourceStringType.MIXED, value, nowPosition, false);
            lastComplete = value.isEmpty();
        }
    }

    private StringIterator skipMultipleCommit(int index, StringIterator sit, SourcePosition oldPosition) {
        return skipTextWithPairedSign(index, sit, oldPosition,
                PropertyConstant.MULTI_LINE_COMMENTS_PRE,
                PropertyConstant.MULTI_LINE_COMMENTS_POST,
                SourceStringType.MULTI_LINE_COMMENTS, false,
                (s, start) -> s.indexOf(PropertyConstant.MULTI_LINE_COMMENTS_POST, start));
    }

    private StringIterator skipString(int index, StringIterator sit, SourcePosition oldPosition) {
        return skipTextWithPairedSign(index, sit, oldPosition,
                String.valueOf(PropertyConstant.STRING_ENCIRCLE_SIGN),
                String.valueOf(PropertyConstant.STRING_ENCIRCLE_SIGN),
                SourceStringType.STRING, true, SimpleTextDecomposer::findTrueStringEnd);
    }

    private static int findTrueStringEnd(String apartText, int start) {
        while (true) {
            if (start > apartText.length()) {
                return -1;
            }
            int stringEndIndex = apartText.indexOf(PropertyConstant.STRING_ENCIRCLE_SIGN, start);
            int fakeStringIndex = apartText.indexOf(FAKE_STRING_CIRCLE_SIGN, start);
            if (stringEndIndex == -1) {
                return -1;
            }
            // 找到了引号
            if (fakeStringIndex == -1) {
                // 从这里获取到最终的行列
                return stringEndIndex;
            }

            start = stringEndIndex + 1;
        }
    }

    private StringIterator skipTextWithPairedSign(
            int index, StringIterator sit, SourcePosition oldPosition,
            String pre, String post, SourceStringType type, boolean add2List,
            BiFunction<String, Integer, Integer> endIndexFinder) {
        int preLen = pre.length();
        int commitPreEnd = index + preLen;
        int commitEnd = endIndexFinder.apply(sit.toString(), commitPreEnd);// sit.toString().indexOf(post, commitPreEnd);
        if (commitEnd == -1) {
            // 不能在这次做完
            add(type, sit.toString().substring(index), oldPosition.clone(0, -preLen), true);
            return null;
        }
        int postLen = post.length();
        String value = sit.toString().substring(index, commitEnd + postLen);
        if (add2List) {
            add(type, value, oldPosition.clone(0, -postLen), true);
        }
        SourcePosition move = SourcePosition.move(oldPosition.clone(0, -postLen), value);
        oldPosition.setRaw(move.getRaw());
        oldPosition.setColumn(move.getColumn());
        return new StringIterator(sit.toString().substring(commitEnd + postLen));
    }

    private StringIterator skipSingleCommit(int index, StringIterator sit, SourcePosition oldPosition) {
        int commitPreEnd = index + PropertyConstant.SINGLE_LINE_COMMENTS_START.length();
        int commitEnd = sit.toString().indexOf(SystemConstant.LINE_SEPARATOR, commitPreEnd);
        if (commitEnd != -1) {
            sit = new StringIterator(sit.toString().substring(
                    commitEnd + SystemConstant.LINE_SEPARATOR.length()
            ));
            if (sit.hasNext()) {
                throw new IllegalStateException("逻辑上不应该出现解析完单行注释已经结束却还没有换行的情况");
            }
            return sit;
        }
        // 只存一个开始
        add(SourceStringType.SINGLE_LINE_COMMENTS,
                PropertyConstant.SINGLE_LINE_COMMENTS_START,
                oldPosition, false
        );
        return null;
    }

    /**
     * 返回老的位置
     */
    private SourcePosition updatePosition(boolean fullLine, String apartText) {
        SourcePosition old = (SourcePosition) nowPosition.clone();
        if (fullLine) {
            nowPosition.rawIncreasing();
        } else {
            nowPosition.columnAdding(apartText.length());
        }
        return old;
    }


    private StringBuilder add(SourceStringType nowStatus, String value, SourcePosition sp, boolean start) {
        if (!value.isEmpty()) {
            phasedText.addLast(new SourceString(nowStatus, value, sp.clone(0, start ? 0 : -value.length())));
        }
        lastComplete = true;
        return new StringBuilder();
    }


    public SourceTextContext get() {
        if (!lastComplete) {
            SourceString last = phasedText.getLast();
            if (last.getStatus() == SourceStringType.SINGLE_LINE_COMMENTS) {
                phasedText.removeLast();
            } else {
                throw new AnalysisException(last.getPosition(), "Need the post part of sign");
            }
        }
        return new SourceTextContext(phasedText);
    }

    public void clear() {
        this.nowPosition.setRaw(0);
        this.nowPosition.setColumn(0);
        phasedText.clear();
    }
}

}
