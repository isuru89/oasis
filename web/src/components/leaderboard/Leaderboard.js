import React, { Component } from 'react'
import styled from 'styled-components';
import Avatar from '../Avatar'
import { formatInt } from "../../utils";
import user from '../../profile.jpg';
import { Scrollbars } from "react-custom-scrollbars";

const Wrapper = styled.div`
  min-width: 300px;
  max-width: 600px;
  user-select: none;
  font-family: 'Roboto';
  background-color: hsl(203, 70%, 20%);
  border-radius: 10px;
  padding-bottom: 10px;
  margin: 20px;
  box-shadow: 0 0px 10px 1px hsla(203, 70%, 20%, 0.2);
`

const HeaderWrapper = styled.div`
  min-height: 200px;
  background-color: hsl(203, 70%, 10%);
  border-radius: 10px 10px 0 0;
  display: flex;
  align-items: center;
  flex-direction: column;
  box-shadow: 0 2px 10px 1px hsla(203, 70%, 20%, 0.7);
`


const LeaderboardTitle = styled.div`
  width: 100%;
  color: hsl(203, 90%, 90%);
  text-align: center;
  font-size: 18px;
  line-height: 48px;
  letter-spacing: 1.5px;
  min-height: 48px;
`

const TopUserName = styled.div`
  text-align: center;
  font-size: 12px;
  min-height: 24px;
  line-height: 24px;
  color: hsl(203, 80%, 90%);
`

const HeaderTopDetails = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  height: 96px;
`

const HeaderRankLabel = styled.div`
  flex: 1;
  color: white;
  font-size: 24px;
  text-align: center;
  line-height: 1;
  letter-spacing: 1px;
`

const HeaderPointLabel = HeaderRankLabel.extend`

`

const ButtonBarWrapper = styled.div`
  color: hsl(203, 70%, 80%);
  display: flex;
  font-size: 12px;
  font-family: 'Roboto';
  font-weight: 300;
  width: 100%;
  margin: 10px 0;
  line-height: 36px;
  border-radius: 10px;
  background-color: hsl(203, 70%, 15%);
  box-shadow: 0px 1px 10px hsl(203, 70%, 20%) inset;
  letter-spacing: 1px;
`

const TimePeriodButton = styled.div`
  width: 25%;
  text-align: center;
  cursor: pointer;

  background-color: ${props => props.selected ? 'hsl(203, 90%, 50%)' : 'inheirted'};
  color: ${props => props.selected ? '#fff': 'inherit'};

  transition-property: background-color;
  transition-duration: .5s;
  transition-timing-function: ease-out;

  &:hover {
    background-color: ${props => props.selected ? 'hsl(203, 90%, 50%)' : 'hsl(203, 90%, 20%)'};
    color: hsl(203, 90%, 90%);
  }
  &:first-child {
    margin-left: 2px;
    border-radius: 10px 0 0 10px;
  }
  &:last-child {
    margin-right: 2px;
    border-radius: 0 10px 10px 0;
  }
  &:not(:last-child) {
    border-right: 1px solid hsl(203, 70%, 20%);
  }
`


const RecordDivSmall = styled.div`
  height: 60px;
  line-height: 60px;
  width: 100%;
  display: flex;
  background-color: hsl(203, 50%, 99%);
  box-shadow: 0 0 10px hsl(203, 50%, 90%) inset;
  box-sizing: content-box;

  &:not(:last-child) {
    border-bottom: 1px solid hsl(203, 20%, 90%);
  }
`

const RankSpan = styled.span`
  width: 48px;
  text-align: center;
  font-size: 22px;
`

const AvatarSpan = styled.span`
  /* line-height: 60px; */
  padding: 0 10px 0 0;
`
const AvatarSpanTop = {
  lineHeight: 120,
  'boxShadow': '0 0 15px 5px hsl(203, 50%, 7%)'
}

const NameSpan = styled.span`
  line-height: 60px;
  padding-right: 10px;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 300;
  color: hsl(203, 90%, 30%);
`

const PointSpan = styled.span`
  padding-right: 15px;
  font-size: 20px;
  line-height: 60px;
  position: absolute;
  right: 0;
  color: hsl(40, 80%, 60%);
`

const HeaderRankValue = styled.div`
  color: hsl(203, 80%, 70%);
  font-weight: 500;
  letter-spacing: -1px;
  font-size: 42px;
`
const HeaderPointValue = styled.div`
  color: hsl(40, 80%, 60%);
  letter-spacing: 1px;
  font-size: 32px;
`

const HeaderLabel = styled.div`
  font-size: 10px;
  font-weight: 300;
  color: hsl(203, 80%, 90%);
`

const timePeriods = [
  { tid: 'daily', label: 'Today' },
  { tid: 'weekly', label: 'This Week' },
  { tid: 'monthly', label: 'This Month' },
  { tid: 'alltime', label: 'All Time' },
]

class LeaderboardRecord extends Component {
  render() {
    const { record, idx, highlighted = false } = this.props;
    return (
      <RecordDivSmall highlighted={highlighted}>
        <RankSpan>{record.rank || idx}</RankSpan>
        <AvatarSpan><Avatar image={user} size={44} border={1} borderColor="hsl(203, 10%, 90%)"/></AvatarSpan>
        <NameSpan>{record.name}</NameSpan>
        <PointSpan>{formatInt(record.points)}</PointSpan>
      </RecordDivSmall>
    )
  }
}

class PeriodButton extends Component {
  render() {
    const { tid, scopePeriod, onClick } = this.props;

    return (
      <TimePeriodButton selected={tid === scopePeriod} onClick={this._triggerClick}>
        {this.props.children}
      </TimePeriodButton>
    );
  }

  _triggerClick = e => {
    if (this.props.onClick) {
      this.props.onClick(this.props.tid);
    }
  }
}

export default class Leaderboard extends Component {

  state = {
    activePeriod: 'daily'
  }

  render() {
    const {
      title,
      records = [],
      headerRecord,
      activePeriod = 'daily',
      whenTimeRangeClicked
    } = this.props;

    return (
      <Wrapper>
        <HeaderWrapper>
          <LeaderboardTitle>{title || 'Leaderboard'}</LeaderboardTitle>
          <HeaderTopDetails>
            <HeaderRankLabel>
              <HeaderLabel>RANK</HeaderLabel>
              <HeaderRankValue>{headerRecord.rank}</HeaderRankValue>
            </HeaderRankLabel>
            <div>
              <Avatar image={user} size={96} style={AvatarSpanTop} />
            </div>
            <HeaderPointLabel>
              <HeaderLabel>POINTS</HeaderLabel>
              <HeaderPointValue>{formatInt(headerRecord.points)}</HeaderPointValue>
            </HeaderPointLabel>
          </HeaderTopDetails>
          <TopUserName>{headerRecord.name}</TopUserName>
          <ButtonBarWrapper>
            {
              timePeriods.map(tp => 
                <PeriodButton key={tp.tid} tid={tp.tid} scopePeriod={activePeriod} onClick={whenTimeRangeClicked}>
                  {tp.label}
                </PeriodButton>
              )
            }
          </ButtonBarWrapper>
        </HeaderWrapper>
        
        <Scrollbars style={{height: 300}} autoHide={true}
          renderThumbVertical={props => <div {...props} 
            style={{backgroundColor: 'hsla(203, 90%, 15%, 0.5)', zIndex: 10, borderRadius: 3}} />}>
          {
            records.map((r, i) => <LeaderboardRecord key={i} record={r} idx={i+2} highlighted={headerRecord.userId === r.userId} />)
          }
        </Scrollbars>
      </Wrapper>
    )
  }
}
