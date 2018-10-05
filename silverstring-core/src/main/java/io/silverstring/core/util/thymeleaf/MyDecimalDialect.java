package io.silverstring.core.util.thymeleaf;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import java.util.HashSet;
import java.util.Set;

public class MyDecimalDialect extends AbstractProcessorDialect {
    public MyDecimalDialect() {
        super( "MyDecimalDialect", "mf", 1000);
    }

    public Set<IProcessor> getProcessors(final String dialectPrefix) {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new MyDecimalAttributeTagProcessor(dialectPrefix));
        return processors;
    }
}
