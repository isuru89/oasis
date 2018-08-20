import React, { Component } from 'react'
import styled from 'styled-components';
import { formatInt } from "../../utils";

const Wrapper = styled.div`
    text-align: right;
`

const RankCell = styled.div`
    line-height: 1;
    font-size: 24px;
    font-weight: bold;
    color: hsl(203, 80%, 56%);
`

const PointsCell = styled.div`
    font-size: 14px;
    color: goldenrod;
    color: hsl(50, 43%, 50%);
`

const PtsLabel = styled.span`
    font-size: 10px;
    margin-left: 5px;
    color: hsl(50, 43%, 30%);
`

export default class RankPoint extends Component {
  render() {
    const { rank, points, suffix = "G", pointStyle, rankStyle } = this.props;

    return (
      <Wrapper>
        <RankCell style={rankStyle}>{rank || '-'}</RankCell>
        <PointsCell style={pointStyle}>{formatInt(points, true, '')} 
        <PtsLabel>{suffix}</PtsLabel></PointsCell>
      </Wrapper>
    )
  }
}
