package io.silverstring.core.util.thymeleaf;

import io.silverstring.domain.util.CompareUtil;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.engine.TemplateModel;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.*;
import org.thymeleaf.templatemode.TemplateMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Set;

@Slf4j
public class MyDecimalAttributeTagProcessor extends AbstractAttributeTagProcessor {
    private static final String ATTR_NAME = "decimal";
    private static final int PRECEDENCE = 10000;

    public MyDecimalAttributeTagProcessor(final String dialectPrefix) {
        super(TemplateMode.HTML, dialectPrefix, null, false, ATTR_NAME, true, PRECEDENCE, true);
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
        final IEngineConfiguration configuration = context.getConfiguration();
        final IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(configuration);
        final IStandardExpression expression = expressionParser.parseExpression(context, attributeValue);
        final Object expressionResult = expression.execute(context, StandardExpressionExecutionContext.RESTRICTED);
        final String unescapedTextStr = (expressionResult == null ? "" : expressionResult.toString());

        String tagValue = new String();
        BigDecimal decimalValue = new BigDecimal(unescapedTextStr).stripTrailingZeros();
        if(CompareUtil.Condition.LT.equals(CompareUtil.compare(decimalValue, new BigDecimal("0.00000001")))) {
            tagValue = "0";
        } else {
            tagValue = decimalValue.toPlainString();
        }

        structureHandler.setBody(tagValue, false);
    }
}
