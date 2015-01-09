package org.jboss.windup.qs.victims.test.rulefilters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;

/**
 * A convenient filter for rule providers enumerated as constructor params.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class PackageRulesFilter implements Predicate<WindupRuleProvider>
{
    private Set<String> packages;


    public PackageRulesFilter(String ... packages)
    {
        this.packages = new HashSet(Arrays.asList(packages));
    }


    @Override
    public boolean accept(WindupRuleProvider ruleProvider)
    {
        return this.packages.contains(ruleProvider.getClass().getPackage().getName());
    }

}// class