#!/usr/bin/env python3
"""
Score prompts with correction penalties applied.

Usage:
    python3 score_prompts_with_corrections.py <ratings.csv> [corrections.json]

Ratings CSV format:
    ID,Timestamp,Prompt_Preview,C1_Clarity,C2_Context,C3_Constraint,C4_Output,C5_Verify,C6_Iterate,C7_Decomp,C8_Source,C9_Role,C10_Example

Corrections JSON format (optional):
    [
        {
            "number": 7,
            "severity": "minor|moderate|major",
            "target_pid": "P1",
            "collateral_pids": []
        }
    ]

If corrections.json is not provided, outputs base scores only.
"""

import csv
import json
import sys

# Principle weights based on user priority: 1, 2, 4, 5, 6, 7, 3, 8, 9, 10
WEIGHTS = {
    'C1': 1.00,  # Clarity - priority 1
    'C2': 0.95,  # Context - priority 2
    'C4': 0.90,  # Output - priority 3
    'C5': 0.85,  # Verify - priority 4
    'C6': 0.80,  # Iterate - priority 5
    'C7': 0.75,  # Decomp - priority 6
    'C3': 0.70,  # Constraint - priority 7
    'C8': 0.65,  # Source - priority 8
    'C9': 0.60,  # Role - priority 9
    'C10': 0.55, # Example - priority 10
}

# Correction penalty configuration
# Base: 2%, Target multiplier: 5×, Additive multiplier: 5×
BASE_PCT = 2.0
TARGET_MULT = 5
ADDITIVE_MULT = 5

PENALTIES = {
    'minor': {
        'target_mult': 1 - (0.5 * BASE_PCT / 100 * TARGET_MULT),     # ×0.95
        'target_add': int(0.5 * BASE_PCT / 2 * ADDITIVE_MULT + 0.5), # +3
        'collateral_mult': 1 - (0.5 * BASE_PCT / 100),                # ×0.99
        'collateral_add': int(0.5 * BASE_PCT / 2 + 0.5)               # +1
    },
    'moderate': {
        'target_mult': 1 - (1.0 * BASE_PCT / 100 * TARGET_MULT),     # ×0.90
        'target_add': int(1.0 * BASE_PCT / 2 * ADDITIVE_MULT + 0.5), # +5
        'collateral_mult': 1 - (1.0 * BASE_PCT / 100),                # ×0.98
        'collateral_add': int(1.0 * BASE_PCT / 2 + 0.5)               # +1
    },
    'major': {
        'target_mult': 1 - (2.0 * BASE_PCT / 100 * TARGET_MULT),     # ×0.80
        'target_add': int(2.0 * BASE_PCT / 2 * ADDITIVE_MULT + 0.5), # +10
        'collateral_mult': 1 - (2.0 * BASE_PCT / 100),                # ×0.96
        'collateral_add': int(2.0 * BASE_PCT / 2 + 0.5)               # +2
    }
}


def calc_score(ratings_row):
    """Calculate pattern score using weighted sum."""
    return sum(ratings_row[c] * WEIGHTS[c] for c in WEIGHTS.keys())


def calc_violation(ratings_row):
    """Calculate anti-pattern violation score using inverted weighted sum."""
    return sum((10 - ratings_row[c]) * WEIGHTS[c] for c in WEIGHTS.keys())


def load_ratings(csv_path):
    """Load prompt ratings from CSV."""
    ratings = {}
    with open(csv_path, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            pid = row['ID']
            ratings[pid] = {
                'C1': int(row['C1_Clarity']),
                'C2': int(row['C2_Context']),
                'C3': int(row['C3_Constraint']),
                'C4': int(row['C4_Output']),
                'C5': int(row['C5_Verify']),
                'C6': int(row['C6_Iterate']),
                'C7': int(row['C7_Decomp']),
                'C8': int(row['C8_Source']),
                'C9': int(row['C9_Role']),
                'C10': int(row['C10_Example']),
                'timestamp': row.get('Timestamp', ''),
                'preview': row.get('Prompt_Preview', '')
            }
    return ratings


def apply_corrections(ratings, corrections):
    """Apply correction penalties to ratings."""
    adjusted_scores = {}
    adjusted_violations = {}

    for pid in ratings.keys():
        base_score = calc_score(ratings[pid])
        base_violation = calc_violation(ratings[pid])

        mult = 1.0
        add = 0

        for corr in corrections:
            p = PENALTIES[corr['severity']]

            if pid == corr['target_pid']:
                mult *= p['target_mult']
                add += p['target_add']
            elif pid in corr.get('collateral_pids', []):
                mult *= p['collateral_mult']
                add += p['collateral_add']

        adjusted_scores[pid] = base_score * mult
        adjusted_violations[pid] = base_violation + add

    return adjusted_scores, adjusted_violations


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)

    ratings_csv = sys.argv[1]
    corrections_json = sys.argv[2] if len(sys.argv) > 2 else None

    # Load ratings
    ratings = load_ratings(ratings_csv)

    # Calculate base scores
    print("=" * 80)
    print("BASE SCORES (before correction penalties)")
    print("=" * 80)

    results = []
    for pid in sorted(ratings.keys()):
        base_score = calc_score(ratings[pid])
        base_violation = calc_violation(ratings[pid])
        results.append({
            'pid': pid,
            'base_score': base_score,
            'base_violation': base_violation,
            'adj_score': base_score,
            'adj_violation': base_violation,
            'preview': ratings[pid]['preview']
        })
        print(f"{pid}: Score {base_score:5.1f} | Violation {base_violation:5.1f} - {ratings[pid]['preview'][:60]}")

    # Apply corrections if provided
    if corrections_json:
        with open(corrections_json, 'r') as f:
            corrections = json.load(f)

        print(f"\n" + "=" * 80)
        print(f"APPLYING {len(corrections)} CORRECTION(S)")
        print("=" * 80)

        for corr in corrections:
            p = PENALTIES[corr['severity']]
            print(f"\nCorrection #{corr['number']} ({corr['severity']}):")
            print(f"  Target: {corr['target_pid']} (×{p['target_mult']:.2f}, +{p['target_add']})")
            if corr.get('collateral_pids'):
                print(f"  Collateral: {', '.join(corr['collateral_pids'])} (×{p['collateral_mult']:.3f}, +{p['collateral_add']})")

        adj_scores, adj_violations = apply_corrections(ratings, corrections)

        # Update results
        for r in results:
            r['adj_score'] = adj_scores[r['pid']]
            r['adj_violation'] = adj_violations[r['pid']]

        print(f"\n" + "=" * 80)
        print("ADJUSTED SCORES (after correction penalties)")
        print("=" * 80)

        for r in results:
            if r['adj_score'] != r['base_score']:
                change_score = r['adj_score'] - r['base_score']
                change_violation = r['adj_violation'] - r['base_violation']
                print(f"{r['pid']}: Score {r['base_score']:5.1f} → {r['adj_score']:5.1f} ({change_score:+5.1f}) | "
                      f"Violation {r['base_violation']:5.1f} → {r['adj_violation']:5.1f} ({change_violation:+5.1f})")

    # Select top patterns and anti-patterns
    print(f"\n" + "=" * 80)
    print("SELECTIONS")
    print("=" * 80)

    by_score = sorted(results, key=lambda x: x['adj_score'], reverse=True)
    by_violation = sorted(results, key=lambda x: x['adj_violation'], reverse=True)

    print("\nTop 7 Patterns:")
    for i, r in enumerate(by_score[:7], 1):
        marker = "⚠️" if r['adj_score'] != r['base_score'] else "  "
        print(f"{i}. {marker} {r['pid']}: {r['adj_score']:5.1f} - {r['preview'][:60]}")

    print("\nTop 5 Anti-patterns:")
    for i, r in enumerate(by_violation[:5], 1):
        marker = "⚠️" if r['adj_violation'] != r['base_violation'] else "  "
        print(f"{i}. {marker} {r['pid']}: {r['adj_violation']:5.1f} - {r['preview'][:60]}")

    # Output for programmatic use
    print(f"\n" + "=" * 80)
    print("OUTPUT (machine-readable)")
    print("=" * 80)
    print(f"PATTERNS: {','.join(r['pid'] for r in by_score[:7])}")
    print(f"ANTI_PATTERNS: {','.join(r['pid'] for r in by_violation[:5])}")


if __name__ == '__main__':
    main()
