package com.example.careerpilot.data.repository

data class CompensationBreakdown(
    val baseSalary: Double,
    val equityTotalGrant: Double,
    val equityVestingYears: Int = 4,
    val signOnBonus: Double,
    val targetBonusPercent: Double,
    val relocationStipend: Double = 0.0
) {
    val annualEquity: Double get() = equityTotalGrant / equityVestingYears
    val annualBonusDollar: Double get() = baseSalary * (targetBonusPercent / 100.0)
    val year1TotalComp: Double get() = baseSalary + annualEquity + signOnBonus + annualBonusDollar + relocationStipend
    val recurringAnnualComp: Double get() = baseSalary + annualEquity + annualBonusDollar
    val fourYearTotalComp: Double get() = (baseSalary * 4) + equityTotalGrant + signOnBonus + (annualBonusDollar * 4) + relocationStipend
    val estimatedMonthlyPreTax: Double get() = year1TotalComp / 12.0
}

data class NegotiationScenario(
    val id: String,
    val title: String,
    val description: String,
    val strategicLeverage: String,
    val counterRecommendation: String,
    val targetAdjustmentFormula: String,
    val emailTemplate: String,
    val verbalScript: String
)

object SalaryNegotiationEngine {

    fun calculateCompensation(
        baseSalary: Double,
        equityGrant: Double,
        signOn: Double,
        bonusPercent: Double,
        relocation: Double = 0.0
    ): CompensationBreakdown {
        return CompensationBreakdown(
            baseSalary = baseSalary,
            equityTotalGrant = equityGrant,
            signOnBonus = signOn,
            targetBonusPercent = bonusPercent,
            relocationStipend = relocation
        )
    }

    val SCENARIOS = listOf(
        NegotiationScenario(
            id = "competing_offer",
            title = "Leverage Competing Offer (Highest Power)",
            description = "You hold an active second offer with a higher base, equity tier, or sign-on bonus.",
            strategicLeverage = "Hiring teams will match or beat verified competing offers to avoid restarting an expensive multi-month recruiting loop.",
            counterRecommendation = "Ask for an increase in base salary by $15,000–$25,000 or an equity grant multiplier of 20% to exceed the competing package.",
            targetAdjustmentFormula = "Base +12% · RSUs +25% · Sign-on +$20,000",
            emailTemplate = """Hi [Recruiter Name],

Thank you so much for extending the offer to join [Company Name] as [Role Title]! I am genuinely excited about the team's roadmap and the engineering challenges we discussed.

I am currently evaluating a concurrent offer from another firm offering a total Year 1 package of $[Target TC] (with a $[Competing Base]k base and $[Competing Equity]k annual equity grant).

Because [Company Name] remains my top choice due to the team culture and technical domain, if we can adjust the base compensation to $[Target Base]k and increase the initial equity grant to $[Target Equity]k, I would be thrilled to sign immediately and withdraw from other processes.

Looking forward to your thoughts!

Best regards,
[Your Name]""",
            verbalScript = "I'm genuinely excited to work together, and [Company] is my #1 choice. However, I have a competing offer at $[TC]. If you can bridge the base to $[Target Base] and round up the equity grant, I'll sign the paperwork today."
        ),

        NegotiationScenario(
            id = "below_market_base",
            title = "Base Salary Below Market Band",
            description = "The offered base salary is below the 75th percentile benchmark for your seniority and tier.",
            strategicLeverage = "Companies have pre-approved salary bands. Asking for the upper quartile with market data is standard practice.",
            counterRecommendation = "Anchor to market benchmarks (Levels.fyi, Radford, Pave) and offer a sign-on bonus compromise if base is hard-capped.",
            targetAdjustmentFormula = "Base +10% to +15% · Or +$25,000 Sign-on Buffer",
            emailTemplate = """Hi [Recruiter Name],

Thank you for putting this offer package together! I'm really looking forward to the opportunity to contribute to [Company Name]'s high-impact goals.

Based on recent market compensation data for Senior Engineers in this tier and my proven background architecting high-throughput distributed systems, I was targeting a base salary in the $[Target Base Min]–$[Target Base Max] range.

Is there flexibility to adjust the base salary to $[Target Base]k? If base salary bands are constrained, I would also be open to structuring a $[Sign-on Amount]k sign-on bonus to bridge the first-year difference.

Thank you again for advocating for me!

Best,
[Your Name]""",
            verbalScript = "Based on my track record optimizing system performance and current market benchmarks for this seniority, I was hoping to land at $[Target Base] base. Is there flexibility within the team's band to make that adjustment?"
        ),

        NegotiationScenario(
            id = "equity_heavy_swap",
            title = "High Growth Equity Multiplier (Pre-IPO / Scaleup)",
            description = "You believe in the company's valuation upside and want to maximize long-term equity / RSUs.",
            strategicLeverage = "Companies love candidates who ask for equity over cash because it signals long-term alignment and preserves company cash reserves.",
            counterRecommendation = "Propose trading $5k–$10k of base salary for a 30%–50% larger RSU grant or an accelerated vesting schedule.",
            targetAdjustmentFormula = "RSU Grant +35% · Accelerated 1-year cliff",
            emailTemplate = """Hi [Recruiter Name],

Thank you again for the offer! I am very bullish on [Company Name]'s growth and mission over the next 4+ years.

Because I want my incentives fully aligned with the company's long-term enterprise value, I would love to explore maximizing the equity portion of the package. Could we adjust the 4-year grant to $[Target Equity Grant] shares/value?

I am ready to commit long-term and build substantial value for the team.

Warmly,
[Your Name]""",
            verbalScript = "I'm exceptionally bullish on where the company is heading. I'd love to have more skin in the game—could we explore increasing the 4-year equity grant to $[Target Equity]?"
        ),

        NegotiationScenario(
            id = "promotion_review_clause",
            title = "6-Month Early Promotion Review Clause",
            description = "If the company cannot budge on base compensation due to strict internal leveling bands.",
            strategicLeverage = "Reduces employer's upfront risk while locking in an expedited path to the next compensation tier upon meeting specific OKRs.",
            counterRecommendation = "Request a formal written clause for a performance and salary compensation review at month 6 rather than month 12.",
            targetAdjustmentFormula = "Written 6-Month Review + $15,000 Sign-on Buffer",
            emailTemplate = """Hi [Recruiter Name],

I appreciate you sharing the constraints regarding the current leveling band. I completely understand.

Given the scope of high-priority milestones I will be driving in my first quarter, could we include a written provision for an expedited performance and salary review at the 6-month mark?

Additionally, could we adjust the initial sign-on bonus to $[SignOn]k to balance the first-year compensation?

Thank you so much!

Best,
[Your Name]""",
            verbalScript = "I understand the band constraints. Would the team be open to adding a formal 6-month compensation and promotion review checkpoint based on delivering our Q1 infrastructure goals?"
        )
    )
}
