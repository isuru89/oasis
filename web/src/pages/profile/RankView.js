import React, { Component } from 'react'
import styled from 'styled-components';

import { formatInt } from '../../utils' 
import { ImageContent } from '../../components/ImageTitleValue';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import RankPoint from './RankPoint';

const Wrapper = styled.div`
  padding: 0 10px;
  background-color: #00000055;
  border-radius: 4px;
`

const Table = styled.table`
  width: 100%;
`

const TableHeader = styled.thead`
  padding: 10px;
  font-weight: light;
  text-transform: uppercase;
  font-size: 20px;
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
              <LabelCell style={{ textAlign: 'left', paddingTop: 10 }}>Rankings</LabelCell>
              <ValueCell></ValueCell>
            </tr>
          </TableHeader>
          <tbody>
            {
              data.map(d => {
                return (
                  <tr key={d.leaderboard}>
                    <LabelCell>
                      <ImageContent image={<FontAwesomeIcon icon="coins" />}>
                        <div>{d.leaderboard}</div>
                      </ImageContent>
                    </LabelCell>
                    <ValueCell>
                      <RankPoint {...d} suffix={<FontAwesomeIcon icon="coins" />} />
                    </ValueCell>
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
