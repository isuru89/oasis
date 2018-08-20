import React, { Component } from 'react'
import styled from 'styled-components';

import { formatInt } from '../../utils' 
import { ImageContent } from '../../components/ImageTitleValue';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import RankPoint from './RankPoint';

const Wrapper = styled.div`
  padding: 0 10px;
  background-color: hsl(203, 50%, 6%);
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

const Row = styled.tr`
  border-bottom: 1px solid hsl(203, 80%, 15%);

  &:last-child {
    border-bottom: none;
  }
`

const LabelCell = styled.td`
  padding: 5px 0;
  font-weight: 300;
`

const LabelCellHeader = styled.td`
  font-weight: 700;
  letter-spacing: 1.5px;
`

const ValueCell = styled.td`
  padding: 5px 0;
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
              <LabelCellHeader style={{ textAlign: 'left', paddingTop: 10 }}>Rankings</LabelCellHeader>
              <ValueCell></ValueCell>
            </tr>
          </TableHeader>
          <tbody>
            {
              data.map(d => {
                return (
                  <Row key={d.leaderboard}>
                    <LabelCell>
                      <ImageContent image={<FontAwesomeIcon icon="list-ol" />}>
                        <div>{d.leaderboard}</div>
                      </ImageContent>
                    </LabelCell>
                    <ValueCell>
                      <RankPoint {...d} suffix={<FontAwesomeIcon icon="coins" />} />
                    </ValueCell>
                  </Row>
                )
              })
            }
          </tbody>
        </Table>
      </Wrapper>
    )
  }
}
