# Ruleset gate
Candidate: Shi Jia rotating-plate Chai Bu (時家轉盤拆補), ID shijia-zhuanpan-chaibu, version 0.0.0-blocked. This candidate is not implemented or independently verified.

Missing authoritative decisions: solar-term ephemeris and precision; midnight versus Zi-hour day boundary; calendrical epoch; Yin/Yang Dun and Ju tables; upper/middle/lower Yuan; leap handling; center hosting; duty-star/door placement; deity order; location/true-solar-time convention. Do not mix schools.

No reference supplied with the build specification establishes these conventions. Public implementations can be investigated but are not themselves adopted as authority. Independent fixture count **0/100**. Until the gate is satisfied, outputs carry BLOCKED with empty palaces and null Dun/Ju. The 3x3 UI is only a fixed Lo Shu position legend.

Required input: an authoritative source for the selected complete convention, plus independently produced worked charts with provenance, timestamps and timezone covering solar-term, midnight, leap and DST boundaries. Engine tests verify determinism and honest blocking, not traditional correctness or predictive validity.
