import React, { Component } from 'react'
import styled from 'styled-components';
import Avatar from '../components/Avatar'
import { formatInt } from "../utils";
import user from '../profile.jpg';
import { Scrollbars } from "react-custom-scrollbars";

const Wrapper = styled.div`
  min-width: 400px;
  border: 1px solid hsl(203, 70%, 30%);
  font-family: 'Roboto';
  background-color: hsl(203, 70%, 10%);
`

const RecordDiv = styled.div`
  height: 50px;
  position: relative;
  margin-bottom: 2px;
  margin-right: 10px;
`
const RecordDivSmall = styled.div`
  height: 34px;
  width: 100%;
  position: absolute;
  top: 8px;
  background-color: hsla(203, 40%, 20%);
  z-index: 1;
`
const RecordDivTop = RecordDiv.extend`
  background-color: hsl(203, 70%, 10%);
  height: 120px;
  margin-right: 0;
`
const RecordDivCentered = styled.div`
  height: 50px;
  background-color: hsl(203, 70%, 30%);
  position: absolute;
  top: 35px;
  right: 0;
  left: 0;
  z-index: 1;
`

const RankSpan = styled.span`
  width: 24px;
  position: absolute;
  left: 0;
  z-index: 3;
  text-align: right;
  line-height: 50px;
`

const AvatarSpan = styled.span`
  padding: 0 5px;
  line-height: 50px;
  position: absolute;
  left: 30px;
  z-index: 3;
`
const AvatarSpanTop = {
  zIndex: 3,
  lineHeight: 120,
  position: 'absolute',
  top: 12,
  left: 5,
}

const NameSpan = styled.span`
  line-height: 50px;
  padding-right: 10px;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  position: absolute;
  left: 90px;
  z-index: 3;
  font-weight: 300;
  color: hsl(203, 90%, 90%);
`
const NameSpanTop = NameSpan.extend`
  font-family: 'Roboto';
  font-size: 20px;
  font-weight: 300;
  padding-left: 5px;
  position: absolute;
  left: 104px;
  line-height: 120px;
  z-index: 3;
  color: hsl(5, 70%, 90%);
`

const PointSpan = styled.span`
  padding-right: 5px;
  font-weight: 500;
  font-size: 16px;
  line-height: 50px;
  position: absolute;
  right: 0;
  z-index: 3;
  color: hsl(40, 80%, 60%);
`
const PointSpanTop = PointSpan.extend`
  font-size: 24px;
  z-index: 3;
  line-height: 120px;
  font-weight: 400;
  color: hsl(40, 80%, 60%);
`


class LeaderboardTopRecord extends Component {
  render() {
    const { record } = this.props;

    return (
      <RecordDivTop>
        <RecordDivCentered />
        <Avatar image={user} size={96} style={AvatarSpanTop} />
        <NameSpanTop>
          {record.name}
        </NameSpanTop>
        <PointSpanTop>
          {formatInt(record.points)}
        </PointSpanTop>
      </RecordDivTop>
    )
  }
}

class LeaderboardRecord extends Component {
  render() {
    const { record, idx } = this.props;
    return (
      <RecordDiv>
        <RecordDivSmall/>
        <RankSpan>{record.rank || idx}</RankSpan>
        <AvatarSpan><Avatar image={user} size={48} border={1} borderColor="#fff"/></AvatarSpan>
        <NameSpan>{record.name}</NameSpan>
        <PointSpan>{formatInt(record.points)}</PointSpan>
      </RecordDiv>
    )
  }
}

export default class Leaderboard extends Component {
  render() {
    const { title, image, records = [] } = this.props;

    return (
      <Wrapper>
        <div>{title}</div>
        <LeaderboardTopRecord record={records[0]} />
        <Scrollbars style={{height: 300}} 
          renderThumbVertical={props => <div {...props} 
            style={{backgroundColor: 'hsla(203, 90%, 35%, 0.5)', zIndex: 10, borderRadius: 3}} />}>
          {
            records.slice(1).map((r, i) => <LeaderboardRecord key={i} record={r} idx={i+2} />)
          }
        </Scrollbars>
      </Wrapper>
    )
  }
}
