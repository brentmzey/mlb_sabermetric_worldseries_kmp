/**
 * MLB Sabermetric World Series Domain Registry & Type Definitions
 * TypeScript Declarations synchronized with docs/schema/mlb_domain_registry.json,
 * Kotlin Multiplatform (KMP), and Python 3.10+.
 */

export type League = "AL" | "NL";

export type Division = "EAST" | "CENTRAL" | "WEST";

export type MlbTeamCode =
  // AL East
  | "NYY" | "BAL" | "BOS" | "TBD" | "TOR"
  // AL Central
  | "CLE" | "KC" | "DET" | "MIN" | "CWS"
  // AL West
  | "HOU" | "SEA" | "TEX" | "OAK" | "LAA"
  // NL East
  | "PHI" | "ATL" | "NYM" | "WSH" | "MIA"
  // NL Central
  | "MIL" | "CHC" | "STL" | "CIN" | "PIT"
  // NL West
  | "LAD" | "SD" | "ARI" | "SF" | "COL";

export type StatPillarType =
  | "OFFENSE"
  | "DEFENSE"
  | "STARTING_PITCHING"
  | "BULLPEN_LEVERAGE";

export type PostseasonRound =
  | "WILD_CARD"
  | "DIVISION_SERIES"
  | "LEAGUE_CHAMPIONSHIP"
  | "WORLD_SERIES";

export type HungarianCollectionPrefix =
  | "i_"
  | "m_"
  | "s_"
  | "o_"
  | "f_";

export type RecordStatusCode =
  | "ACTIVE"
  | "INACTIVE"
  | "SUPERSEDED"
  | "ARCHIVED";

export interface TeamFranchiseMetadata {
  readonly code: MlbTeamCode;
  readonly fullName: string;
  readonly league: League;
  readonly division: Division;
  readonly city: string;
  readonly ballpark: string;
  readonly foundedYear: number;
  readonly mlbApiId: number;
}

export interface StatPillarDefinition {
  readonly code: StatPillarType;
  readonly name: string;
  readonly weight: number;
  readonly primaryMetric: string;
  readonly secondaryMetric: string;
  readonly description: string;
}

export interface PostseasonRoundDefinition {
  readonly code: PostseasonRound;
  readonly name: string;
  readonly bestOf: number;
  readonly winsToAdvance: number;
  readonly homeFieldFormat: string;
}

export interface WorldSeriesLeaderboardEntry {
  readonly rank: number;
  readonly teamCode: MlbTeamCode;
  readonly teamName: string;
  readonly league: League;
  readonly division: Division;
  readonly expectedWins: number;
  readonly playoffProb: number;
  readonly pennantProb: number;
  readonly worldSeriesWinProb: number;
  readonly visualBar?: string;
  readonly isActive: boolean;
  readonly statusCode: RecordStatusCode;
  readonly updatedEpochMsUtc: number;
}
