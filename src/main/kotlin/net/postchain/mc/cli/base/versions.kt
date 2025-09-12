package net.postchain.mc.cli.base

const val DIRECTORY_CHAIN_ECONOMY_CHAIN_VERSION = 30L // The version of directory chain introducing economy chain

const val CLUSTER_ANCHORING_CHAIN_RESOURCE_USAGE_VERSION = 3L

const val ECONOMY_CHAIN_COMMON_PROPOSAL_VERSION = 21L // EC <= v20 use ec proposals, v21 > use common_proposals
const val ECONOMY_CHAIN_PROVIDER_MULTI_KEY_VERSION = 43L
const val ECONOMY_CHAIN_STAKING_REQUIREMENTS_VERSION = 24L // EC >= 24 has staking requirements
const val ECONOMY_CHAIN_EC_CONSTANTS_AS_PROPOSALS_VERSION = 29L // EC >= 29 has changed EC constants to be proposals instead of admin controlled
const val ECONOMY_CHAIN_EC_STAKING_REQ_AND_USD_MINOR_UNITS_VERSION = 33L // EC >= 29 has changed EC constants to be proposals instead of admin controlled
const val ECONOMY_CHAIN_PRICE_ORACLE_RATE_PROPOSAL_VERSION = 34L // EC >= 34 has price oracle rate proposal
const val ECONOMY_CHAIN_STAKING_REQ_NODE_BASED_VERSION = 46L
const val ECONOMY_CHAIN_SCHEDULED_PROPOSAL_VERSION = 53L
const val ECONOMY_CHAIN_REQUIRE_PROVIDER_IDENTIFIER_AND_DYNAMIC_CU_VERSION = 57L
const val ECONOMY_CHAIN_COMPUTE_REQUESTS = 60L
const val ECONOMY_CHAIN_MAX_CLUSTER_NODES_VERSION = 63L
