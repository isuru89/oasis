import React, { Component } from 'react'
import styled from 'styled-components';

import { formatInt } from '../../utils' 

const Wrapper = styled.div`
  padding: 0 10px;
`

const Table = styled.table`
  width: 100%;
`

const TableHeader = styled.thead`
  padding: 10px;
  font-weight: bold;
  text-transform: uppercase;
`

const LabelCell = styled.td`

`

const ValueCell = styled.td`
  text-align: right;
`

export default class RankView extends Component {
  render() {
    const { data = [] } = this.props;

    return (
      <Wrapper>
        <Table>
          <TableHeader>
            <tr>
              <LabelCell style={{ paddingBottom: 5 }}>Leaderboard</LabelCell>
              <ValueCell>Gold</ValueCell>
              <ValueCell>Rank</ValueCell>
            </tr>
          </TableHeader>
          <tbody>
            {
              data.map(d => {
                return (
                  <tr>
                    <LabelCell>{d.leaderboard}</LabelCell>
                    <ValueCell>{formatInt(d.points)}</ValueCell>
                    <ValueCell>{d.rank}</ValueCell>
                  </tr>
                )
              })
            }
          </tbody>
        </Table>
      </Wrapper>
    )
  }
}
