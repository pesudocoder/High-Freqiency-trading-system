package com.hft.dto;

import java.util.List;

public record MarketDepthSnapshotDTO(List<DepthLevel> bids, List<DepthLevel> asks) {}
