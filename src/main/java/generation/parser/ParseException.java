package generation.parser;

import org.parboiled.errors.DefaultInvalidInputErrorFormatter;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.errors.ParseError;
import org.parboiled.support.Position;

import java.util.List;

public class ParseException extends IllegalArgumentException {
    private static final long serialVersionUID = -2624556764086947780L;

    private volatile List<ParseError> errors;

    public ParseException(List<ParseError> errors) {
        super(buildMessage(errors));
        this.errors = errors;
    }

    private static String buildMessage(List<ParseError> errors) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot build a ParseException without a list of errors");
        }

        StringBuilder sb = new StringBuilder();
        for (ParseError pe : errors) {
            Position pos = pe.getInputBuffer().getPosition(pe.getStartIndex());
            String message =
                    pe.getErrorMessage() != null
                            ? pe.getErrorMessage()
                            : pe instanceof InvalidInputError
                            ? new DefaultInvalidInputErrorFormatter()
                            .format((InvalidInputError) pe)
                            : pe.getClass().getSimpleName();
            sb.append(message)
                    .append(" (line ")
                    .append(pos.line)
                    .append(", column ")
                    .append(pos.column)
                    .append(")");
            sb.append('\n');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    /**
     * The parse errors
     *
     * @return
     */
    public List<ParseError> getErrors() {
        return errors;
    }
}
