import React, { Component } from 'react'
import styled from 'styled-components';
import { formatInt } from "../../utils";

const Wrapper = styled.div`
  padding: 4px 10px 10px;
`

const PointRow = styled.tr`

`

const TotalRow = styled.tr`
  border-bottom: 2px solid hsl(50, 83%, 56%);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
`

const LabelColumn = styled.td`
  width: 80%;
  font-size: 14px;
`

const TotalColumn = LabelColumn.extend`
  font-size: 16px;
`

const ValueColumn = styled.td`
  text-align: right;
  font-size: 14px;
`

export default class PointBreakdown extends Component {
  render() {
    const { total = 0, records = [] } = this.props;

    return (
      <Wrapper>
        <table style={{ width: '100%' }}>
          <tbody>
          <TotalRow>
            <TotalColumn>Total</TotalColumn>
            <ValueColumn>{formatInt(total, false)}</ValueColumn>
          </TotalRow>
          </tbody>
        </table>
        <table style={{ width: '100%', marginTop: 5 }}>
          <tbody>
            {
              records.map(r => 
                <PointRow key={r.label}>
                  <LabelColumn>{r.label}</LabelColumn>
                  <ValueColumn>{formatInt(r.points)}</ValueColumn>
                </PointRow>
              )
            }
          </tbody>
        </table>
      </Wrapper>
    )
  }
}
